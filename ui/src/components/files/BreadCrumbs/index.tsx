import "./index.css"

export default function Breadcrumbs(
    {
        names,
        selectFn = (_) => {},
    }: {
        names: string[],
        selectFn?: ((n: number) => any)
    }) {

    return <ul className={"breadcrumbs"}>
        {names.map((name, index) => <>
                <li
                    className="breadcrumb"
                    onClick={e => {
                        e.preventDefault();
                        selectFn(index)
                    }}
                >
                    {name}
                </li>
            <li className={"breadcrumbs_delimiter"}>
                {">"}
            </li>
            </>
        )}
    </ul>
}