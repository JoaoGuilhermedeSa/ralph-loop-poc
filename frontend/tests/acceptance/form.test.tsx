/**
 * The frontend oracle. Pins the contract in specs/04-frontend.md.
 * Ralph may not edit this file. See AGENTS.md.
 */
import { render, screen, waitFor, within } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";

import App from "../../src/App";

const VOCATIONS = [
  { id: 0, name: "None" },
  { id: 1, name: "Sorcerer" },
  { id: 2, name: "Druid" },
  { id: 3, name: "Paladin" },
  { id: 4, name: "Knight" },
];

const TOWNS = [
  { id: 1, name: "Thais" },
  { id: 2, name: "Carlin" },
  { id: 3, name: "Venore" },
];

const CREATED = {
  id: 1,
  accountId: 1,
  name: "Bubble",
  vocation: 1,
  vocationName: "Sorcerer",
  sex: 1,
  townId: 1,
  townName: "Thais",
  level: 8,
  experience: 4200,
  health: 185,
  healthMax: 185,
  mana: 210,
  manaMax: 210,
  capacity: 470,
  lookType: 130,
};

type Handler = (url: string, init?: RequestInit) => { status: number; body: unknown };

let postResponse: Handler;
let fetchCalls: Array<{ url: string; init?: RequestInit }>;

function json(status: number, body: unknown) {
  return {
    ok: status >= 200 && status < 300,
    status,
    json: async () => body,
  } as Response;
}

beforeEach(() => {
  fetchCalls = [];
  postResponse = () => ({ status: 201, body: CREATED });

  vi.stubGlobal("fetch", vi.fn(async (input: RequestInfo | URL, init?: RequestInit) => {
    const url = String(input);
    fetchCalls.push({ url, init });
    if (url.includes("/api/vocations")) return json(200, VOCATIONS);
    if (url.includes("/api/towns")) return json(200, TOWNS);
    if (url.includes("/api/characters")) {
      const result = postResponse(url, init);
      return json(result.status, result.body);
    }
    throw new Error(`unexpected fetch to ${url}`);
  }));
});

afterEach(() => {
  vi.unstubAllGlobals();
  vi.restoreAllMocks();
});

/** Waits for the two reference-data requests to settle. */
async function renderLoaded() {
  render(<App />);
  await waitFor(() => {
    expect(within(screen.getByTestId("field-vocation")).getAllByRole("option").length).toBe(
      VOCATIONS.length,
    );
  });
}

function lastPostBody() {
  const call = [...fetchCalls].reverse().find((c) => c.url.includes("/api/characters"));
  if (!call?.init?.body) throw new Error("no POST to /api/characters was made");
  return JSON.parse(String(call.init.body));
}

describe("character creation form", () => {
  it("renders every field from the spec", async () => {
    await renderLoaded();
    expect(screen.getByTestId("field-name")).toBeInTheDocument();
    expect(screen.getByTestId("field-vocation")).toBeInTheDocument();
    expect(screen.getByTestId("field-sex-female")).toBeInTheDocument();
    expect(screen.getByTestId("field-sex-male")).toBeInTheDocument();
    expect(screen.getByTestId("field-town")).toBeInTheDocument();
    expect(screen.getByTestId("submit")).toBeInTheDocument();
  });

  it("populates vocations and towns from the API", async () => {
    await renderLoaded();
    const vocationOptions = within(screen.getByTestId("field-vocation")).getAllByRole("option");
    expect(vocationOptions.map((o) => o.textContent)).toEqual(VOCATIONS.map((v) => v.name));
    const townOptions = within(screen.getByTestId("field-town")).getAllByRole("option");
    expect(townOptions.map((o) => o.textContent)).toEqual(TOWNS.map((t) => t.name));
  });

  it("defaults to Sorcerer, male and Thais", async () => {
    await renderLoaded();
    expect(screen.getByTestId("field-vocation")).toHaveValue("1");
    expect(screen.getByTestId("field-sex-male")).toBeChecked();
    expect(screen.getByTestId("field-town")).toHaveValue("1");
  });

  it("labels every control", async () => {
    await renderLoaded();
    expect(screen.getByTestId("field-name")).toHaveAccessibleName();
    expect(screen.getByTestId("field-vocation")).toHaveAccessibleName();
    expect(screen.getByTestId("field-town")).toHaveAccessibleName();
  });

  it("rejects a too-short name without calling the API", async () => {
    await renderLoaded();
    const user = userEvent.setup();
    await user.type(screen.getByTestId("field-name"), "Ab");
    await user.click(screen.getByTestId("submit"));
    expect(await screen.findByTestId("name-error")).toBeInTheDocument();
    expect(fetchCalls.some((c) => c.url.includes("/api/characters"))).toBe(false);
  });

  it("rejects a name containing digits without calling the API", async () => {
    await renderLoaded();
    const user = userEvent.setup();
    await user.type(screen.getByTestId("field-name"), "Player 1");
    await user.click(screen.getByTestId("submit"));
    expect(await screen.findByTestId("name-error")).toBeInTheDocument();
    expect(fetchCalls.some((c) => c.url.includes("/api/characters"))).toBe(false);
  });

  it("posts the chosen values", async () => {
    await renderLoaded();
    const user = userEvent.setup();
    await user.type(screen.getByTestId("field-name"), "Bubble");
    await user.selectOptions(screen.getByTestId("field-vocation"), "3");
    await user.click(screen.getByTestId("field-sex-female"));
    await user.selectOptions(screen.getByTestId("field-town"), "2");
    await user.click(screen.getByTestId("submit"));

    await waitFor(() => expect(lastPostBody()).toMatchObject({
      name: "Bubble",
      vocation: 3,
      sex: 0,
      townId: 2,
      accountId: 1,
    }));
  });

  it("shows the created character on 201", async () => {
    await renderLoaded();
    const user = userEvent.setup();
    await user.type(screen.getByTestId("field-name"), "Bubble");
    await user.click(screen.getByTestId("submit"));

    const result = await screen.findByTestId("result");
    expect(result).toHaveTextContent("Bubble");
    expect(result).toHaveTextContent("Sorcerer");
    expect(result).toHaveTextContent("Thais");
    expect(result).toHaveTextContent("185");
    expect(result).toHaveTextContent("210");
    expect(result).toHaveTextContent("470");
  });

  it("shows the server message when the name is taken", async () => {
    postResponse = () => ({
      status: 409,
      body: { error: "NAME_TAKEN", message: "That name is already taken.", field: "name" },
    });
    await renderLoaded();
    const user = userEvent.setup();
    await user.type(screen.getByTestId("field-name"), "Bubble");
    await user.click(screen.getByTestId("submit"));

    expect(await screen.findByTestId("server-error")).toHaveTextContent(
      "That name is already taken.",
    );
    expect(screen.getByTestId("field-name")).toHaveAttribute("aria-invalid", "true");
    expect(screen.queryByTestId("result")).not.toBeInTheDocument();
  });

  it("keeps what the player typed after a server error", async () => {
    postResponse = () => ({
      status: 409,
      body: { error: "NAME_TAKEN", message: "That name is already taken.", field: "name" },
    });
    await renderLoaded();
    const user = userEvent.setup();
    await user.type(screen.getByTestId("field-name"), "Bubble");
    await user.click(screen.getByTestId("submit"));
    await screen.findByTestId("server-error");
    expect(screen.getByTestId("field-name")).toHaveValue("Bubble");
  });

  it("shows a retry message when the network fails", async () => {
    postResponse = () => {
      throw new Error("network down");
    };
    await renderLoaded();
    const user = userEvent.setup();
    await user.type(screen.getByTestId("field-name"), "Bubble");
    await user.click(screen.getByTestId("submit"));
    expect(await screen.findByTestId("server-error")).toBeInTheDocument();
  });

  it("returns to an empty form from the result panel", async () => {
    await renderLoaded();
    const user = userEvent.setup();
    await user.type(screen.getByTestId("field-name"), "Bubble");
    await user.click(screen.getByTestId("submit"));
    await screen.findByTestId("result");

    await user.click(screen.getByTestId("create-another"));
    await waitFor(() => expect(screen.getByTestId("field-name")).toHaveValue(""));
    expect(screen.queryByTestId("result")).not.toBeInTheDocument();
  });
});
