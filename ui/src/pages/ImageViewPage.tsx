import {useEffect, useId} from "react";
import "./ImageViewPage.css"

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
        let cleanupSource = imageZoom(sourceIdR, zoomSourceIdR);
        let cleanupOptimized = imageZoom(optimizedIdR, zoomOptimizedIdR)
        return () => {
            cleanupSource();
            cleanupOptimized()
        }
    })

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
        <div className={"img-zoom-container"}>
            <div className={"flex"}>
                <div>
                    {renderImage(sourceIdR, sourceUrl)}
                </div>
                <div>
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

function getMouseCoords(e: MouseEvent | TouchEvent): { x: number, y: number } {
    let coords: { x: number, y: number };
    if (e instanceof TouchEvent) {
        let touch = e.touches[0] || e.changedTouches[0];
        coords = {x: touch.pageX, y: touch.pageY}
    } else {
        coords = {x: e.pageX, y: e.pageY}
    }
    return coords;
}

function createLensForImage(img: HTMLImageElement) {
    /*create lens:*/
    let lens = document.createElement("DIV");
    lens.setAttribute("class", "img-zoom-lens");
    lens.setAttribute("id", img.id + "_lens")

    /*insert lens:*/
    img.parentElement!!.insertBefore(lens, img);
    return lens;
}

function imageZoom(imgID: string, resultID: string) {
    let img: HTMLImageElement = document.getElementById(imgID) as HTMLImageElement;
    let result = document.getElementById(resultID) as HTMLElement;

    let lens = createLensForImage(img);

    /*calculate the ratio between result DIV and lens:*/
    let cx = result.offsetWidth / lens.offsetWidth;
    let cy = result.offsetHeight / lens.offsetHeight;
    console.log({
        ow: result.offsetWidth,
        low: lens.offsetWidth
    })
    console.log({cx, cy})

    /*set background properties for the result DIV:*/
    result.style.backgroundImage = "url('" + img.src + "')";
    result.style.backgroundSize = (img.width * cx) + "px " + (img.height * cy) + "px";

    /*execute a function when someone moves the cursor over the image, or the lens:*/
    lens.addEventListener("click", moveLens);
    img.addEventListener("click", moveLens);

    /*and also for touch screens:*/
    // lens.addEventListener("touchmove", moveLens);
    // img.addEventListener("touchmove", moveLens);
    // img.removeEventListener("mousemove", moveLens)

    function moveLens(e: MouseEvent | TouchEvent) {
        /*prevent any other actions that may occur when moving over the image:*/
        e.preventDefault();

        let mouseCoords = getMouseCoords(e);

        console.log("click " + img.id + " _ " + lens.id)
        /*get the cursor's x and y positions:*/
        let pos = getRelativeCursorPos(img, mouseCoords);
        /*prevent the lens from being positioned outside the image:*/

        /*calculate the position of the lens:*/
        let x = bound(0, pos.x - (lens.offsetWidth / 2), img.width - lens.offsetWidth);
        let y = bound(0, pos.y - (lens.offsetHeight / 2), img.height - lens.offsetHeight);
        /*set the position of the lens:*/
        lens.style.left = x + "px";
        lens.style.top = y + "px";
        /*display what the lens "sees":*/
        console.log({x, y})
        let backgroundPosition = "-" + (x * cx) + "px -" + (y * cy) + "px";
        console.log(backgroundPosition)
        result.style.backgroundPosition = backgroundPosition;
    }

    return function cleanup() {
        img.removeEventListener("mousemove", moveLens)
        lens.remove()
    }

}

function getRelativeCursorPos(
    img: HTMLImageElement,
    mouseCoords: { x: number; y: number }
) {
    /*get the x and y positions of the image:*/
    let imgBorderCoordinates: DOMRect = img.getBoundingClientRect();
    /*calculate the cursor's x and y coordinates, relative to the image:*/
    let x: number = mouseCoords.x - window.scrollX - imgBorderCoordinates.left;
    let y: number = mouseCoords.y - window.scrollY - imgBorderCoordinates.top;
    console.log({x, y})
    return {x, y};
}

/**
 * Returns min(value, upperBound(min, value))
 * @param lowerBound
 * @param value
 * @param upperBound
 */
function bound(lowerBound: number, value: number, upperBound: number): number {
    if (value > upperBound) {
        return upperBound;
    }
    if (value < lowerBound) {
        return lowerBound;
    }
    return value
}

// function getRelativeCursorPos2(img: HTMLImageElement, e: MouseEvent) {
//     e = e || window.event as MouseEvent; // TODO ?
//     /*get the x and y positions of the image:*/
//     let imgBorderCoordinates: DOMRect = img.getBoundingClientRect();
//     /*calculate the cursor's x and y coordinates, relative to the image:*/
//     let x: number = e.pageX - imgBorderCoordinates.left;
//     let y: number = e.pageY - imgBorderCoordinates.top;
//     /*consider any page scrolling:*/
//     x = x - window.scrollX;
//     y = y - window.scrollY;
//     console.log({x, y})
//     return {x, y};
// }
