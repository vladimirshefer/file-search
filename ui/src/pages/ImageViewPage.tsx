import {useEffect, useId} from "react";
import "./ImageViewPage.css"
import MultipleImageZoomer from "lib/image_zoom/image_zoomer";

export default function ImageViewPage() {
    let sourceId = useId();
    let optimizedId = useId();
    let zoomSourceId = useId();
    let zoomOptimizedId = useId();

    let sourceIdR = `source${sourceId}`;
    let optimizedIdR = `optimized${optimizedId}`;
    let zoomSourceIdR = `zoomSource${zoomSourceId}`;
    let zoomOptimizedIdR = `zoomOptimized${zoomOptimizedId}`;

    useEffect(() => {
        let imageZoomer = new MultipleImageZoomer([
            {imageId: sourceIdR, viewId: zoomSourceIdR},
            {imageId: optimizedIdR, viewId: zoomOptimizedIdR},
        ]);
        imageZoomer.mount()
        return () => {
            imageZoomer.unmount();
        }
    }, [])

    let sourceUrl = "https://cdn.pixabay.com/photo/2015/04/23/22/00/tree-736885__480.jpg";
    let optimizedUrl = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcThJv66ZNB5dva_CLQC49ZgiCIxhzsjN0VJPigpZNE0AGR_-2Svj39J3tm_gDH0WdDUgDE&usqp=CAU";

    function renderImage(id: string, src: string) {
        return <img id={id}
                    draggable={false}
                    className={"img-zoom-img"}
                    alt={"Source image"}
                    src={src}
        />;
    }

    return <>
        <div>
            <div className={"flex"}>
                <div className={"img-zoom-container"}>
                    {renderImage(sourceIdR, sourceUrl)}
                </div>
                <div className={"img-zoom-container"}>
                    {renderImage(optimizedIdR, optimizedUrl)}
                </div>
            </div>
            <div className={"flex"}>
                <div id={zoomSourceIdR} className={"img-zoom-result"}/>
                <div id={zoomOptimizedIdR} className={"img-zoom-result"}/>
            </div>
        </div>
    </>
}
