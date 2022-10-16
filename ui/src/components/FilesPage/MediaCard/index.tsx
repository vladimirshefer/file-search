import React from "react";
import "./index.css"
import {MediaStatus} from "lib/Api";

export default function MediaCard(
    {
        name,
        path,
        status,
        isSelected,
        actionOpen = () => undefined,
        actionSelect = () => undefined,
    }: {
        name: string,
        path: string,
        status: MediaStatus,
        isSelected: boolean
        actionOpen?: () => void
        actionSelect?: () => void
    }) {
    return (
        <li className={"media-card" + " " + (isSelected?"media-card__selected":"")} title={name}
            onDoubleClick={actionOpen}
            onClick={actionSelect}
        >
            <div className={"media-card_image"}
                 style={{backgroundImage: "url('/api/files/show/?path=" + path + "/" + name + "')"}}
            />
            <span className={"media-card_name"}>
            {name} {status}
        </span>
        </li>
    )
}
