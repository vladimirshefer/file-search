import "./Sidebar.css"
import { ReactNode } from "react";
import { RiCloseFill } from "react-icons/ri";

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

    return <div className={"sidebar w-full h-full p-10 fixed z-50 top-0 left-0 grid"}>
        <button className={"bg-black/30 px-2 py-1 rounded-full fixed top-3 right-3 text-white z-[51] text-lg"}
            type={"button"}
            onClick={actionClose}
            tabIndex={0}
        >
            <RiCloseFill/>
        </button>
        <div className={"sidebar_background"}
            onClick={actionClose}
        />
        <div
            className={`sidebar_body bg-white overflow-hidden place-self-center w-full h-full grid place-content-center ${isVisible ? "" : "hidden"}`}>
            {children}
        </div>
    </div>
}
