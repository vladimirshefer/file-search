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
                <li key={name}>
                    <button
                        className="breadcrumb"
                        onClick={e => {
                            e.preventDefault();
                            selectFn(index)
                        }}
                        title={name}
                    >
                        {name}
                    </button>
                </li>
                <li className={"last:hidden mx-1"}
                    key={name + "_delimiter"}>
                    {"/"}
                </li>
            </>
        )}
    </ul>
}
