import { useEffect, useState, type FormEvent } from "react";
import "./App.css";

interface Vocation {
  id: number;
  name: string;
}

interface Town {
  id: number;
  name: string;
}

export default function App() {
  const [vocations, setVocations] = useState<Vocation[]>([]);
  const [towns, setTowns] = useState<Town[]>([]);
  const [loadingReferenceData, setLoadingReferenceData] = useState(true);

  const [name, setName] = useState("");
  const [vocation, setVocation] = useState(1);
  const [sex, setSex] = useState(1);
  const [townId, setTownId] = useState(1);

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

  function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
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

        <button type="submit" data-testid="submit" disabled={loadingReferenceData}>
          Create character
        </button>
      </form>
    </div>
  );
}
