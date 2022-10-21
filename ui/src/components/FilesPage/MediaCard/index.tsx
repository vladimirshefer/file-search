import React, {useState} from "react";
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
        actionOpenSource = () => undefined,
        actionOpenOptimized = () => undefined,
        actionDeleteSource = () => undefined,
        actionDeleteOptimized = () => undefined,
    }: {
        name: string,
        path: string,
        status: MediaStatus,
        isSelected: boolean
        actionOpenSource?: () => void
        actionOpenOptimized?: () => void
        actionDeleteSource?: () => void
        actionDeleteOptimized?: () => void
    }) {

    let [isOptionsOpened, setOptionsOpened] = useState<boolean>(false)

    return (<>
            <li className={`media-card ${isSelected ? "media-card__selected" : ""}`}
                title={name}
                onDoubleClick={actionOpenSource}
                data-selection-id={name} // used for drag-select.
                key={name}
            >
                <div className={"media-card_content"}>
                    <div className={"media-card_image"}
                         style={{backgroundImage: "url('/api/files/show/?path=" + path + "/" + name + "')"}}
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
                        <li onClick={actionOpenSource}>Open source</li>
                        <li onClick={actionOpenOptimized}>Open optimized</li>
                        <li onClick={actionDeleteSource}>Delete source</li>
                        <li onClick={actionDeleteOptimized}>Delete optimized</li>
                    </ul>
                </div>
            </li>
        </>
    )
}
