package com.ff.eq;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class EquationApiTest {

  @Autowired MockMvc mvc;

  private void store(String eq) throws Exception {
    mvc.perform(post("/api/equations/store").contentType(MediaType.APPLICATION_JSON)
        .content("{\"equation\":\"" + eq + "\"}"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.equationId").isNumber());
  }

  @Test void storeThenListShowsInfixAndPostfix() throws Exception {
    store("3x + 2y - z");
    mvc.perform(get("/api/equations"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.equations[?(@.postfix=='3 x * 2 y * + z -')].equation").value(org.hamcrest.Matchers.hasItem("3 * x + 2 * y - z")));
  }

  @Test void evaluateStoredEquation() throws Exception {
    mvc.perform(post("/api/equations/store").contentType(MediaType.APPLICATION_JSON)
        .content("{\"equation\":\"3x + 2y - z\"}"))
      .andExpect(status().isOk());
    // id of the equation just stored is the highest id in the list
    String body = mvc.perform(get("/api/equations")).andReturn().getResponse().getContentAsString();
    java.util.regex.Matcher m = java.util.regex.Pattern.compile("\"equationId\":(\\d+)").matcher(body);
    long id = 0; while (m.find()) id = Math.max(id, Long.parseLong(m.group(1)));
    mvc.perform(post("/api/equations/" + id + "/evaluate").contentType(MediaType.APPLICATION_JSON)
        .content("{\"variables\":{\"x\":2,\"y\":3,\"z\":1}}"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.result").value(11.0));
  }

  @Test void complexEquationEvaluates() throws Exception {
    store("(a + 2b)^2 / 4 - -c");
    String body = mvc.perform(get("/api/equations")).andReturn().getResponse().getContentAsString();
    java.util.regex.Matcher m = java.util.regex.Pattern.compile("\"equationId\":(\\d+)").matcher(body);
    long id = 0; while (m.find()) id = Math.max(id, Long.parseLong(m.group(1)));
    // (2+2*3)^2/4 + 1 = 64/4 + 1 = 17
    mvc.perform(post("/api/equations/" + id + "/evaluate").contentType(MediaType.APPLICATION_JSON)
        .content("{\"variables\":{\"a\":2,\"b\":3,\"c\":1}}"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.result").value(17.0));
  }

  @Test void invalidEquationReturns400() throws Exception {
    mvc.perform(post("/api/equations/store").contentType(MediaType.APPLICATION_JSON)
        .content("{\"equation\":\"(2+3\"}"))
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.error").value("Unbalanced parentheses"));
  }

  @Test void blankEquationReturns400() throws Exception {
    mvc.perform(post("/api/equations/store").contentType(MediaType.APPLICATION_JSON)
        .content("{\"equation\":\"\"}"))
      .andExpect(status().isBadRequest());
  }

  @Test void missingVariableReturns400() throws Exception {
    store("p + q");
    String body = mvc.perform(get("/api/equations")).andReturn().getResponse().getContentAsString();
    java.util.regex.Matcher m = java.util.regex.Pattern.compile("\"equationId\":(\\d+)").matcher(body);
    long id = 0; while (m.find()) id = Math.max(id, Long.parseLong(m.group(1)));
    mvc.perform(post("/api/equations/" + id + "/evaluate").contentType(MediaType.APPLICATION_JSON)
        .content("{\"variables\":{\"p\":1}}"))
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.error").value("Missing value for variable 'q'"));
  }

  @Test void unknownIdReturns404() throws Exception {
    mvc.perform(post("/api/equations/99999/evaluate").contentType(MediaType.APPLICATION_JSON)
        .content("{\"variables\":{}}"))
      .andExpect(status().isNotFound());
  }
}