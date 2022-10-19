import React from "react";
import "./index.css"
import {MediaStatus} from "lib/Api";
import {IoCheckmarkDoneOutline} from "react-icons/io5";
import {AiOutlineCheck, AiOutlineClose} from "react-icons/ai";

export default function MediaCard(
    {
        name,
        path,
        status,
        isSelected,
        actionOpen = () => undefined,
    }: {
        name: string,
        path: string,
        status: MediaStatus,
        isSelected: boolean
        actionOpen?: () => void
    }) {
    return (
        <li className={"media-card" + " " + (isSelected ? "media-card__selected" : "")} title={name}
            onDoubleClick={actionOpen}
            data-selection-id={name} // used for drag-select.
        >
            <div className={"media-card_image"}
                 style={{backgroundImage: "url('/api/files/show/?path=" + path + "/" + name + "')"}}
            />
            <div className={"media-card_info"}>
                <span className={"media-card_icon"}>
                    {
                        status == "OPTIMIZED_ONLY" ? <IoCheckmarkDoneOutline/> :
                            status == "OPTIMIZED" ? <AiOutlineCheck/> :
                                status == "SOURCE_ONLY" ? <AiOutlineClose/> : null
                    }
                </span>
                <span className={"media-card_name"}>
                    {name}
                </span>
            </div>
        </li>
    )
}
