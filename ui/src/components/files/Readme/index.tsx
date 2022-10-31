import "./index.css"

export function Readme(
    {
        readme
    }: {
        readme?: string | null | undefined
    }
) {
    console.log(readme)

    return <div className={`readme ${readme ? "" : "hidden"}`}>
        <h3>README</h3>
        <pre className={"readme_text"}>
                {readme}
        </pre>
    </div>;
}
