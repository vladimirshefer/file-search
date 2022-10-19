import {MediaInfo} from "lib/Api";
import MediaCard from "components/FilesPage/MediaCard";
import React from "react";
import "./index.css"

export default function MediaCardGrid(
    {
        imageMedias,
        path,
        selectedItems,
        actionOpen = (_) => undefined,
    }: {
        imageMedias: MediaInfo[],
        path: string,
        selectedItems: string[],
        actionOpen?: (name: string) => void
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
                    isSelected={selectedItems.includes(it.displayName)}
                    actionOpen={() => actionOpen(it.displayName)}
                />
            })
        }
    </ul>
}
