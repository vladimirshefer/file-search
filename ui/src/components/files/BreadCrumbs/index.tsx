import "./index.css"
import "components/toolbox/Toolbox.css"
import { Fragment } from "react";

export default function Breadcrumbs(
    {
        names,
        selectFn = (_) => null,
    }: {
        names: string[],
        selectFn?: ((n: number) => any)
    }) {

    return <ul className={"flex ml-3"}>
        {names.map((name, index) => <Fragment key={name}>
                <li>
                    <button
                        className="breadcrumb"
                        onClick={e => {
                            e.preventDefault();
                            e.stopPropagation()
                            selectFn(index)
                        }}
                        title={name}
                    >
                        {name}
                    </button>
                </li>
                <li className={"last:hidden mx-1"}>
                    {"/"}
                </li>
            </Fragment>
        )}
    </ul>
}
