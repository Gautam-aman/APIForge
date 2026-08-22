import { useState } from "react";

function App() {

    const [apiDefinition, setApiDefinition] = useState("");

    return (
        <div className="app">

            <header className="header">
                <h1>APIForge</h1>

                <p>
                    Learn, test and improve APIs with AI.
                </p>
            </header>

            <main className="container">

                <section className="hero">

                    <h2>
                        Analyze your API
                    </h2>

                    <p>
                        Paste an OpenAPI specification or API definition
                        and let APIForge analyze it.
                    </p>

                    <textarea
                        value={apiDefinition}
                        onChange={(e) =>
                            setApiDefinition(e.target.value)
                        }
                        placeholder="Paste your OpenAPI specification here..."
                    />

                    <button>
                        Analyze API
                    </button>

                </section>

            </main>

        </div>
    );
}

export default App;