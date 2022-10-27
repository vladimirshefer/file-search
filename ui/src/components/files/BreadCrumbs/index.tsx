import "./index.css"
import "components/toolbox/Toolbox.css"

export default function Breadcrumbs(
    {
        names,
        selectFn = (_) => null,
    }: {
        names: string[],
        selectFn?: ((n: number) => any)
    }) {

    return <ul className={"flex ml-3"}>
        {names.map((name, index) => <>
                <li
                    className="breadcrumb"
                    onClick={e => {
                        e.preventDefault();
                        selectFn(index)
                    }}
                    title={name}
                    key={name}
                >
                    <a>
                        {name}
                    </a>
                </li>
                <li className={"last:hidden mx-1"}
                    key={name + "_delimiter"}>
                    {"/"}
                </li>
            </>
        )}
    </ul>
}
