import {MediaInfo} from "lib/Api";
import MediaCard from "components/FilesPage/MediaCard";
import React from "react";
import "./index.css"

export default function MediaCardGrid(
    {
        imageMedias,
        path,
        actionOpen = (_) => {},
    }: {
        imageMedias: MediaInfo[],
        path: string,
        actionOpen?: (nane: string) => void
    }
) {
    return <ul className={"media-card-grid"}>
        {
            imageMedias.map(it => {
                return <MediaCard
                    key={it.displayName}
                    name={it.displayName}
                    path={path}
                    status={it.status}
                    actionOpen={() => actionOpen(it.displayName)}
                />
            })
        }
    </ul>
}
