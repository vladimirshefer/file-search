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
        actionDeleteSource = (_) => undefined,
        actionDeleteOptimized = (_) => undefined,
    }: {
        imageMedias: MediaInfo[],
        path: string,
        selectedItems: string[],
        actionOpen?: (name: string) => void
        actionDeleteSource?: (name: string) => void
        actionDeleteOptimized?: (name: string) => void
    }
) {

    return <ul className={"media-card-grid"}>
        <div className={"media-card-grid_header"}>
            Media ({imageMedias.length})
        </div>
        {
            imageMedias.map(it => {
                return <MediaCard
                    key={it.displayName}
                    name={it.displayName}
                    path={path}
                    status={it.status}
                    isSelected={selectedItems.includes(it.displayName)}
                    actionOpen={() => actionOpen(it.displayName)}
                    actionDeleteSource={() => actionDeleteSource(it.displayName)}
                    actionDeleteOptimized={() => actionDeleteOptimized(it.displayName)}
                />
            })
        }
    </ul>
}
