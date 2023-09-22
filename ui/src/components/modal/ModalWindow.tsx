import { ReactNode } from "react";
import { RiCloseFill } from "react-icons/ri";

export default function ModalWindow(
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

    return <div className={"sidebar w-full h-full p-10 fixed z-50 top-0 left-0 grid overflow-hidden"}>
        <button className={"bg-black/30 px-2 py-1 rounded-full fixed top-3 right-3 text-white z-[51] text-lg"}
            type={"button"}
            onClick={actionClose}
            tabIndex={0}
        >
            <RiCloseFill/>
        </button>
        <div className={"sidebar_background z-[49] fixed top-0 left-0 h-full w-full bg-black/50"}
            onClick={actionClose}
        />
        <div
            className={`sidebar_body z-50 bg-white rounded-md overflow-hidden p-3 place-self-center w-full h-full grid place-content-center ${isVisible ? "" : "hidden"}`}>
            {children}
        </div>
    </div>
}
