# Frontend

React 19 + Vite + TypeScript. One route, one form. No router, no state
library, no component framework — this is a single page with four inputs.

## Behaviour

On mount, fetch `GET /api/vocations` and `GET /api/towns` and populate the two
selects. While that is in flight, the submit button is disabled.

The form has four controls:

| control  | element                     | `data-testid`     |
|----------|-----------------------------|-------------------|
| name     | `<input type="text">`       | `field-name`      |
| vocation | `<select>`                  | `field-vocation`  |
| sex      | two `<input type="radio">`  | `field-sex-female`, `field-sex-male` |
| town     | `<select>`                  | `field-town`      |
| submit   | `<button type="submit">`    | `submit`          |

Defaults: name empty, vocation `1` (Sorcerer), sex `1` (male), town `1` (Thais).

## Client-side validation

Mirror rules 1-6 from `02-name-rules.md` on the name field. On submit, an
invalid name renders the error and the request is **not sent**; the button
itself stays enabled, so pressing it always explains what is wrong rather than
silently doing nothing. This is a courtesy, not a security boundary: the
server validates independently and its answer always wins.

The message for a client-side failure is rendered in
`data-testid="name-error"`. Use the same wording the server uses so the player
never sees two different sentences for the same mistake.

## Submitting

`POST /api/characters` with the four fields plus the `accountId` the page was
given (for now, read it from `VITE_ACCOUNT_ID`, defaulting to `1` — real
session handling is out of scope, see `00-overview.md`).

- **201** — replace the form with a result panel, `data-testid="result"`,
  showing name, vocation name, town name, level, health, mana and capacity.
  A "Create another" button, `data-testid="create-another"`, returns to an
  empty form.
- **400 / 404 / 409** — render `message` from the error body in
  `data-testid="server-error"`. If `field` is `"name"`, also mark the name
  input `aria-invalid="true"`. The form keeps what the player typed.
- **network failure** — render a generic retry message in the same element.
  Never a blank screen, never a thrown error boundary.

The submit button is disabled while a request is in flight and shows
`Creating…`.

## Accessibility

Every control has a `<label>` bound by `htmlFor`. The error elements are
`role="alert"`. Keyboard submit works. This is four inputs; there is no excuse
for it not to be perfect.

## Look

Dark, Tibia-ish, restrained: a parchment-on-slate card, gold accent for the
submit button, the character's stats in a monospaced column in the result
panel. Do not import a UI kit or a CSS framework for four fields.
