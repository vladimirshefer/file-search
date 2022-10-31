import "./Sidebar.css"
import {ReactNode} from "react";

export default function Sidebar(
    {
        isVisible,
        children,
        actionClose = () => {
        },
    }: {
        isVisible?: boolean
        children?: ReactNode,
        actionClose?: () => void,
    }
) {

    if (!isVisible) return null;

    return <div className={"sidebar"}>
        <div className={"sidebar_background"}
             onClick={actionClose}
        />
        <div className={"sidebar_body sidebar_body__right " + (isVisible ? "" : "hidden")}>
            <button
                type={"button"}
                onClick={actionClose}
                tabIndex={0}
            >
                Close
            </button>
            {children}
        </div>
    </div>
}
