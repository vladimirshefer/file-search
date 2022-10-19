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

    return <ul className={"breadcrumbs"}>
        {names.map((name, index) => <>
                <li
                    className="breadcrumb toolbox_item"
                    onClick={e => {
                        e.preventDefault();
                        selectFn(index)
                    }}
                    key={name}
                >
                    {name}
                </li>
                <li className={"breadcrumbs_delimiter toolbox_delimiter"}
                    key={name + "_delimiter"}>
                    {">"}
                </li>
            </>
        )}
    </ul>
}
