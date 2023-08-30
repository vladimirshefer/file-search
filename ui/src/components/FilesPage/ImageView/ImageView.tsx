import { useEffect, useId, useState } from "react";
import "./ImageView.css"
import MultipleImageZoomer from "lib/image_zoom/image_zoomer";

export default function ImageView(
    {
        image1Url = "https://cdn.pixabay.com/photo/2015/04/23/22/00/tree-736885__480.jpg",
        image2Url = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcThJv66ZNB5dva_CLQC49ZgiCIxhzsjN0VJPigpZNE0AGR_-2Svj39J3tm_gDH0WdDUgDE&usqp=CAU",
    }: {
        image1Url: string,
        image2Url: string,
    }) {
    let sourceId = useId();
    let optimizedId = useId();
    let zoomSourceId = useId();
    let zoomOptimizedId = useId();
    let [imageZoomer, setImageZoomer] = useState<MultipleImageZoomer>()

    let sourceIdR = `source${sourceId}`;
    let optimizedIdR = `optimized${optimizedId}`;
    let zoomSourceIdR = `zoomSource${zoomSourceId}`;
    let zoomOptimizedIdR = `zoomOptimized${zoomOptimizedId}`;

    useEffect(() => {
        let imageZoomer = new MultipleImageZoomer([
            {imageId: sourceIdR, viewId: zoomSourceIdR},
            {imageId: optimizedIdR, viewId: zoomOptimizedIdR},
        ]);
        setImageZoomer(imageZoomer)
        imageZoomer.mount()
        return () => {
            imageZoomer.unmount();
        }
    }, [sourceIdR, optimizedIdR, zoomSourceIdR, zoomOptimizedIdR, image1Url, image2Url])

    function renderImage(id: string, src: string) {
        return <img id={id}
                    draggable={false}
                    className={"img-zoom-img"}
                    alt={"Source"}
                    src={src}
        />;
    }

    return <>
        <div className={"image-view"}
             onWheel={(e) => {
                 e.preventDefault()
                 e.stopPropagation()
                 let zoomIn = e.deltaY < 0;
                 let zoomStep = zoomIn ? 1.2 : 1 / 1.2;
                 imageZoomer?.setZoomRate(imageZoomer?.getZoomRate() * zoomStep)
                 return;
             }}>
            <div className={"flex"}>
                <div className={"img-zoom-container"}>
                    {renderImage(sourceIdR, image1Url)}
                </div>
                <div className={"img-zoom-container"}>
                    {renderImage(optimizedIdR, image2Url)}
                </div>
            </div>
            <div className={"flex"}>
                <div id={zoomSourceIdR} className={"img-zoom-result"}/>
                <div id={zoomOptimizedIdR} className={"img-zoom-result"}/>
            </div>
        </div>
    </>
}
