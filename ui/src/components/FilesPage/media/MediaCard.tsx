import React, {KeyboardEventHandler, useMemo, useState} from "react";
import "./MediaCard.css"
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
    let toggleOptionsOpened = () => setOptionsOpened(!isOptionsOpened);

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
                                status === "OPTIMIZED_ONLY" ? <IoCheckmarkDoneOutline/> :
                                    status === "OPTIMIZED" ? <AiOutlineCheck/> :
                                        status === "SOURCE_ONLY" ? <AiOutlineClose/> : null
                            }
                        </span>
                        <span className={"media-card_name"}
                           tabIndex={0}
                           onKeyUp={(e) => {
                               if (e.key === "Enter") actionOpen()
                           }}
                        >
                            {name}
                        </span>
                        <button className={"media-card_options-button"}
                           onClick={prevent(toggleOptionsOpened)}
                           onKeyUp={ifEnter(toggleOptionsOpened)}
                           onTouchEnd={prevent(toggleOptionsOpened)}
                           onDoubleClick={prevent(null)}
                        >
                            ...
                        </button>
                    </div>
                </div>
                <div className={`media-card_options ${isOptionsOpened ? "" : "hidden"}`}
                     key={name + "_options"}
                     onMouseLeave={(e) => {
                         setTimeout(() => setOptionsOpened(false), 500)
                     }}
                >
                    <ul>
                        <li>
                            <button
                                onClick={actionOpen}
                                onTouchEnd={actionOpen}
                                onKeyUp={ifEnter(actionOpen)}
                            >
                                Open
                            </button>
                        </li>
                        <li>
                            <button
                                onClick={actionDeleteSource}
                                onTouchEnd={actionDeleteSource}
                                onKeyUp={ifEnter(actionOpen)}
                            >
                                Delete source
                            </button>
                        </li>
                        <li>
                            <button
                                onClick={actionDeleteOptimized}
                                onTouchEnd={actionDeleteOptimized}
                                onKeyUp={ifEnter(actionOpen)}
                            >
                                Delete optimized
                            </button>
                        </li>
                    </ul>
                </div>
            </li>
        </>
    )
}

function ifEnter<T>(action: () => any): KeyboardEventHandler<T> {
    return prevent((e: KeyboardEvent) => {
            if (e.key === "Enter" || e.key === "Space") action()
            return undefined
        }
    )
}

function prevent<E extends Event, EH>(action: ((e: E) => any) | null): EH {
    return ((e: E) => {
        e.preventDefault()
        e.stopPropagation()
        return action && action(e)
    }) as EH
}
