import React from "react";
import "./index.css"
import {MediaStatus} from "lib/Api";

export default function MediaCard(
    {
        name,
        path,
        status,
        actionOpen = () => {},
    }: {
        name: string,
        path: string,
        status: MediaStatus,
        actionOpen: () => void
    }) {
    return <li className={"media-card"} title={name}
        onDoubleClick={actionOpen}>
        <div className={"media-card_image"}
             style={{backgroundImage: "url('/api/files/show/?path=" + path + "/" + name + "')"}}
        />
        <span className={"media-card_name"}>
            {name} {status}
        </span>
    </li>
}
