import { useEffect, useState, type FormEvent } from "react";
import "./App.css";
import { normalizeName, validateName } from "./nameValidation";

interface Vocation {
  id: number;
  name: string;
}

interface Town {
  id: number;
  name: string;
}

interface CreatedCharacter {
  name: string;
  vocationName: string;
  townName: string;
  level: number;
  health: number;
  healthMax: number;
  mana: number;
  manaMax: number;
  capacity: number;
}

const ACCOUNT_ID = Number(import.meta.env.VITE_ACCOUNT_ID ?? "1");

export default function App() {
  const [vocations, setVocations] = useState<Vocation[]>([]);
  const [towns, setTowns] = useState<Town[]>([]);
  const [loadingReferenceData, setLoadingReferenceData] = useState(true);

  const [name, setName] = useState("");
  const [vocation, setVocation] = useState(1);
  const [sex, setSex] = useState(1);
  const [townId, setTownId] = useState(1);

  const [nameError, setNameError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);
  const [result, setResult] = useState<CreatedCharacter | null>(null);

  useEffect(() => {
    let cancelled = false;

    async function loadReferenceData() {
      const [vocationsResponse, townsResponse] = await Promise.all([
        fetch("/api/vocations"),
        fetch("/api/towns"),
      ]);
      const [vocationsBody, townsBody] = await Promise.all([
        vocationsResponse.json() as Promise<Vocation[]>,
        townsResponse.json() as Promise<Town[]>,
      ]);
      if (cancelled) return;
      setVocations(vocationsBody);
      setTowns(townsBody);
      setLoadingReferenceData(false);
    }

    void loadReferenceData();
    return () => {
      cancelled = true;
    };
  }, []);

  function resetForm() {
    setResult(null);
    setName("");
    setVocation(1);
    setSex(1);
    setTownId(1);
    setNameError(null);
  }

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();

    const error = validateName(name);
    if (error) {
      setNameError(error);
      return;
    }
    setNameError(null);

    setSubmitting(true);
    try {
      const response = await fetch("/api/characters", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          accountId: ACCOUNT_ID,
          name: normalizeName(name),
          vocation,
          sex,
          townId,
        }),
      });
      const body = await response.json();
      if (response.ok) {
        setResult(body as CreatedCharacter);
      }
      // non-2xx handling (server-error, aria-invalid): next backlog item.
    } catch {
      // network-failure handling (server-error retry message): next backlog item.
    } finally {
      setSubmitting(false);
    }
  }

  if (result) {
    return (
      <div className="page">
        <div className="card" data-testid="result">
          <h1>{result.name}</h1>
          <dl className="stats">
            <dt>Vocation</dt>
            <dd>{result.vocationName}</dd>
            <dt>Town</dt>
            <dd>{result.townName}</dd>
            <dt>Level</dt>
            <dd>{result.level}</dd>
            <dt>Health</dt>
            <dd>
              {result.health} / {result.healthMax}
            </dd>
            <dt>Mana</dt>
            <dd>
              {result.mana} / {result.manaMax}
            </dd>
            <dt>Capacity</dt>
            <dd>{result.capacity}</dd>
          </dl>
          <button type="button" data-testid="create-another" onClick={resetForm}>
            Create another
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="page">
      <form className="card" onSubmit={handleSubmit}>
        <h1>Create your character</h1>

        <div className="field">
          <label htmlFor="field-name">Name</label>
          <input
            id="field-name"
            data-testid="field-name"
            type="text"
            value={name}
            onChange={(event) => setName(event.target.value)}
          />
          {nameError && (
            <p className="error" data-testid="name-error" role="alert">
              {nameError}
            </p>
          )}
        </div>

        <div className="field">
          <label htmlFor="field-vocation">Vocation</label>
          <select
            id="field-vocation"
            data-testid="field-vocation"
            value={vocation}
            onChange={(event) => setVocation(Number(event.target.value))}
          >
            {vocations.map((v) => (
              <option key={v.id} value={v.id}>
                {v.name}
              </option>
            ))}
          </select>
        </div>

        <fieldset className="field">
          <legend>Sex</legend>
          <label htmlFor="field-sex-female">
            <input
              id="field-sex-female"
              data-testid="field-sex-female"
              type="radio"
              name="sex"
              checked={sex === 0}
              onChange={() => setSex(0)}
            />
            Female
          </label>
          <label htmlFor="field-sex-male">
            <input
              id="field-sex-male"
              data-testid="field-sex-male"
              type="radio"
              name="sex"
              checked={sex === 1}
              onChange={() => setSex(1)}
            />
            Male
          </label>
        </fieldset>

        <div className="field">
          <label htmlFor="field-town">Town</label>
          <select
            id="field-town"
            data-testid="field-town"
            value={townId}
            onChange={(event) => setTownId(Number(event.target.value))}
          >
            {towns.map((t) => (
              <option key={t.id} value={t.id}>
                {t.name}
              </option>
            ))}
          </select>
        </div>

        <button type="submit" data-testid="submit" disabled={loadingReferenceData || submitting}>
          {submitting ? "Creating…" : "Create character"}
        </button>
      </form>
    </div>
  );
}
