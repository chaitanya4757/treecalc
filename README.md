# TreeCalc

A Spring Boot REST API that stores algebraic equations as postfix expression trees, returns them in infix form, and evaluates them with variable values.

## Tech
Java 21, Spring Boot 3.3, springdoc-openapi (Swagger), JUnit 5

## Run locally
```
mvn spring-boot:run
```
Swagger UI: http://localhost:8080/swagger-ui.html

## Endpoints
| Method | Path | Purpose |
|---|---|---|
| POST | /api/equations/store | Store an equation, e.g. `{"equation":"3x + 2y - z"}` |
| GET | /api/equations | List equations in infix form |
| POST | /api/equations/{id}/evaluate | Evaluate with `{"variables":{"x":2,"y":3,"z":1}}` |

## Live demo
- App: https://treecalc.onrender.com
- Swagger: https://treecalc.onrender.com/swagger-ui.html

(Free Render instance: the first request after idle can take about 50 seconds.)

## How it works
1. **Tokenizer** splits the text into numbers, variables, operators and brackets. Implicit multiplication is handled, so `3x` becomes `3 * x`.
2. **Shunting-yard** converts the tokens to postfix, e.g. `3x + 2y - z` becomes `3 x * 2 y * + z -`. It supports precedence, right-associative `^`, brackets and unary minus.
3. **Tree builder** reads the postfix with a stack and builds the expression tree (`Num`, `Var`, `Neg`, `Bin` nodes).
4. **Store** keeps the trees in memory, keyed by id.
5. **List** does an inorder traversal of each tree to give the infix form back.
6. **Evaluate** does a recursive postorder evaluation using the variable values.

## Error handling
| Case | Response |
|---|---|
| Invalid syntax, e.g. `(2+3`, `2 +`, `2 $ 3` | 400 with a message |
| Missing variable value | 400 |
| Division by zero | 400 |
| Unknown equation id | 404 |

## Tests
```
mvn test
```
- `EquationTest`: parser and evaluator, covering precedence, `^`, unary minus, implicit multiplication, errors and division by zero.
- `EquationApiTest`: store, list and evaluate through the API, plus the 400 and 404 cases.

## Assumptions
- Numbers are `double`, and whole numbers are printed without `.0`.
- Variables are letters, e.g. `x`, `rate`.
- Storage is in memory, so data resets when the app restarts.
- The sample output in the assignment PDF shows 10 for `3x + 2y - z` with x=2, y=3, z=1. The correct result is 11 (6 + 6 - 1), and that is what this app returns.

## Test with Postman / curl
Base URL: `https://treecalc.onrender.com` (or `http://localhost:8080`)

1. **Store**: `POST /api/equations/store`
   Body (raw JSON): `{"equation":"3x + 2y - z"}`
2. **List**: `GET /api/equations`
3. **Evaluate**: `POST /api/equations/1/evaluate`
   Body (raw JSON): `{"variables":{"x":2,"y":3,"z":1}}`

curl example:
```
curl -X POST https://treecalc.onrender.com/api/equations/store \
  -H "Content-Type: application/json" \
  -d '{"equation":"3x + 2y - z"}'
```