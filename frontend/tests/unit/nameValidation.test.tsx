import { describe, expect, it } from "vitest";

import { normalizeName, validateName } from "../../src/nameValidation";

describe("normalizeName", () => {
  it("trims leading and trailing whitespace", () => {
    expect(normalizeName("  Bubble  ")).toBe("Bubble");
  });

  it("collapses internal whitespace runs to a single space", () => {
    expect(normalizeName("Test   Char")).toBe("Test Char");
  });

  it("preserves case", () => {
    expect(normalizeName("eLiTe kNiGhT")).toBe("eLiTe kNiGhT");
  });
});

describe("validateName", () => {
  it("accepts a valid name", () => {
    expect(validateName("Bubble")).toBeNull();
  });

  it("rejects a name shorter than 3 characters", () => {
    expect(validateName("Ab")).toBe("Name must be at least 3 characters long.");
  });

  it("rejects a name longer than 29 characters", () => {
    expect(validateName("A".repeat(30))).toBe("Name must be at most 29 characters long.");
  });

  it("rejects digits", () => {
    expect(validateName("Player 1")).toBe("Name may only contain letters and single spaces.");
  });

  it("rejects more than 4 words", () => {
    expect(validateName("One Two Three Four Five")).toBe("Name may have at most 4 words.");
  });

  it("rejects a word shorter than 2 characters", () => {
    expect(validateName("Bo B")).toBe("Every word in the name must be at least 2 characters long.");
  });

  it("rejects a reserved word as a substring, case-insensitively", () => {
    expect(validateName("Godlike")).toBe("Name contains a reserved word.");
    expect(validateName("Gamemaster Bob")).toBe("Name contains a reserved word.");
  });

  it("rejects a reserved word hidden by whitespace", () => {
    expect(validateName("Go dl ike")).toBe("Name contains a reserved word.");
  });

  it("does not reject two-letter abbreviations like gm", () => {
    expect(validateName("Sigmund")).toBeNull();
  });

  it("checks rules in order: word-too-short beats reserved word", () => {
    expect(validateName("Godfrey X")).toBe("Every word in the name must be at least 2 characters long.");
  });

  it("checks rules in order: too-short beats invalid-characters", () => {
    expect(validateName("A1")).toBe("Name must be at least 3 characters long.");
  });
});
