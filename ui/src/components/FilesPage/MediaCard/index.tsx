import React, {useMemo, useState} from "react";
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
        actionDeleteSource = () => undefined,
        actionDeleteOptimized = () => undefined,
    }: {
        name: string,
        path: string,
        status: MediaStatus,
        isSelected: boolean,
        actionOpen?: () => void,
        actionDeleteSource?: () => void,
        actionDeleteOptimized?: () => void,
    }) {

    let [isOptionsOpened, setOptionsOpened] = useState<boolean>(false)

    let previewBackgroundUrl = useMemo(() => {
        let thumbnailUrl = `/api/files/show/?rootName=thumbnails,optimized,source&path=${path}/${name}`;
        return `url('${thumbnailUrl}')`;
    }, []);

    return (<>
            <li className={`media-card ${isSelected ? "media-card__selected" : ""}`}
                title={name}
                onDoubleClick={actionOpen}
                data-selection-id={name} // used for drag-select.
                key={name}
            >
                <div className={"media-card_content"}>
                    <div className={"media-card_image"}
                         style={{backgroundImage: previewBackgroundUrl}}
                    />
                    <div className={`media-card_info drag-selectable`}
                         data-selection-id={name} // used for drag-select.
                    >
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
                        <span className={"media-card_options-button"}
                              onClick={(e) => {
                                  e.preventDefault();
                                  setOptionsOpened(!isOptionsOpened)
                              }}
                              onTouchEnd={(e) => {
                                  e.preventDefault()
                                  setOptionsOpened(!isOptionsOpened)
                              }}
                              onDoubleClick={(e) => {
                                  e.preventDefault()
                              }}
                        >
                            ...
                        </span>
                    </div>
                </div>
                <div className={`media-card_options ${isOptionsOpened ? "" : "hidden"}`}
                     key={name + "_options"}
                     onMouseLeave={(e) => {
                         setTimeout(() => setOptionsOpened(false), 500)
                     }}
                >
                    <ul>
                        <li
                            onClick={actionOpen}
                            onTouchEnd={actionOpen}
                        >
                            Open
                        </li>
                        <li
                            onClick={actionDeleteSource}
                            onTouchEnd={actionDeleteSource}
                        >
                            Delete source
                        </li>
                        <li
                            onClick={actionDeleteOptimized}
                            onTouchEnd={actionDeleteOptimized}
                        >
                            Delete optimized
                        </li>
                    </ul>
                </div>
            </li>
        </>
    )
}
