// Client-side mirror of backend/src/main/java/ots/charcreate/character/NameValidator.java
// and specs/02-name-rules.md rules 1-6. Rule 7 (uniqueness) is server-only.
// Messages match the backend's wording exactly, per specs/04-frontend.md:
// "the player never sees two different sentences for the same mistake."

const MIN_LENGTH = 3;
const MAX_LENGTH = 29;
const MAX_WORDS = 4;
const MIN_WORD_LENGTH = 2;
const ALLOWED_CHARACTERS = /^[A-Za-z]+( [A-Za-z]+)*$/;
const RESERVED_WORDS = [
  "god",
  "admin",
  "administrator",
  "gamemaster",
  "tutor",
  "counsellor",
  "counselor",
  "staff",
  "owner",
  "support",
  "system",
  "null",
  "undefined",
];

export function normalizeName(rawName: string): string {
  return rawName.trim().replace(/\s+/g, " ");
}

export function validateName(rawName: string): string | null {
  const name = normalizeName(rawName);

  if (name.length < MIN_LENGTH) {
    return "Name must be at least 3 characters long.";
  }
  if (name.length > MAX_LENGTH) {
    return "Name must be at most 29 characters long.";
  }
  if (!ALLOWED_CHARACTERS.test(name)) {
    return "Name may only contain letters and single spaces.";
  }

  const words = name.split(" ");
  if (words.length > MAX_WORDS) {
    return "Name may have at most 4 words.";
  }
  if (words.some((word) => word.length < MIN_WORD_LENGTH)) {
    return "Every word in the name must be at least 2 characters long.";
  }

  const withoutSpaces = name.replace(/ /g, "").toLowerCase();
  if (RESERVED_WORDS.some((reserved) => withoutSpaces.includes(reserved))) {
    return "Name contains a reserved word.";
  }

  return null;
}
