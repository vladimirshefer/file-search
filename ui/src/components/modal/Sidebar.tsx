import "./Sidebar.css"
import {ReactNode} from "react";

export default function Sidebar(
    {
        isVisible,
        children,
        actionClose = () => {},
    }: {
        isVisible?: boolean
        children?: ReactNode,
        actionClose?: () => void,
    }
) {

    if (!isVisible) return null;

    return <>
        <div className={"sidebar_background"}
             onClick={actionClose}
        />
        <div className={"sidebar sidebar__right " + (isVisible ? "" : "hidden")}>
            {children}
        </div>
    </>
}
