/**
 * Class that allows to select image and set the div
 * element as the magnifying glass for this image.
 * If several elements are passed to the constructor,
 * then several zooms will be created and their position will be synchronized
 */
export default class MultipleImageZoomer {

    zoomers: SingleImageZoomer[] = [];

    setCurrentPosition(x: number, y: number) {
        this.zoomers.forEach(zoomer => {
            zoomer.setPositionPercent(x, y)
        })
    }

    getZoomRate(): number {
        return this.zoomers[0].getZoomRate()
    }

    setZoomRate(zoomRate: number) {
        this.zoomers.forEach(zoomer => {
            zoomer.setZoomRate(zoomRate)
        })
    }

    constructor(elements: { imageId: string, viewId: string, controlId?: string }[] = []) {
        this.zoomers = elements.map(element =>
            new SingleImageZoomer(
                element.imageId,
                element.viewId,
                (x, y) => this.setCurrentPosition(x, y)
            )
        )
    }

    mount() {
        this.zoomers.forEach(zoomer => zoomer.mount())
    }

    unmount() {
        this.zoomers.forEach(zoomer => zoomer.unmount())
    }

}

/**
 * Class that allows to select image and set the div
 * element as the magnifying glass for this image.
 */
export class SingleImageZoomer {
    private readonly imageId: string
    private readonly viewId: string
    private lensId!: string
    private image!: HTMLImageElement
    private view!: HTMLElement
    private lens!: HTMLElement
    private zoomRate: number = 3

    private readonly positionListener: (x: number, y: number) => void

    private isRunning: boolean = true
    private readonly id = Math.floor(Math.random() * 100000)

    private position: { x: number, y: number } = {x: 0.5, y: 0.5};


    /**
     * @param zoomRate >=1.
     */
    setZoomRate = (zoomRate: number) => {
        this.zoomRate = zoomRate
        this.updateLensSize();
        this.updateLensPosition()
        this.updateViewBackgroundSize()
        this.updateViewBackgroundPosition()
    };

    getZoomRate: () => number = () => this.zoomRate;

    constructor(
        imageId: string,
        viewId: string,
        positionListener: (x: number, y: number) => void
    ) {
        if (!imageId) throw "Zoomer: imageId is not set"
        if (!viewId) throw `Zoomer: viewId is not set for image ${imageId}`
        this.imageId = imageId;
        this.viewId = viewId;
        this.positionListener = positionListener
    }

    readonly mount = () => {
        console.log(`Zoomer ${this.id} ${this.imageId}: mount init`)
        this.isRunning = true;
        this.image = document.getElementById(this.imageId) as HTMLImageElement
        this.view = document.getElementById(this.viewId) as HTMLElement
        this.waitAndMount()
    };

    readonly unmount = () => {
        console.log(`Zoomer ${this.id} ${this.imageId}: unmount`)
        this.isRunning = false;

        this.lens?.removeEventListener("mousemove", this.moveLens);
        this.image?.removeEventListener("mousemove", this.moveLens);

        /*and also for touch screens:*/
        this.lens?.removeEventListener("touchmove", this.moveLens);
        this.image?.removeEventListener("touchmove", this.moveLens);
        this.lens?.remove()
    };

    private waitAndMount = () => {
        if (!this.isRunning) {
            console.log(`Zoomer ${this.id} ${this.imageId}: is unmounted. Mount cancelled.`)
            return
        }
        console.log(`Zoomer ${this.id} ${this.imageId}: Trying to mount if loaded`)
        if (!this.imageId) console.log(this)
        if (this.image?.complete) {
            this.doMount()
        } else {
            console.log(`Zoomer ${this.id} ${this.imageId}: Image is not ready... waiting...`)
            setTimeout(this.waitAndMount, 300)
        }
    }

    private doMount = () => {
        this.lens = this.createLensForImage();
        this.view.style.backgroundImage = "url('" + this.image.src + "')";
        this.updateViewBackgroundSize();

        this.lens.addEventListener("mousemove", this.moveLens);
        this.image.addEventListener("mousemove", this.moveLens);

        /*and also for touch screens:*/
        this.lens.addEventListener("touchmove", this.moveLens);
        this.image.addEventListener("touchmove", this.moveLens);
    }

    private createLensForImage() {
        /*create lens:*/
        let lens = document.createElement("DIV");
        lens.setAttribute("class", "img-zoom-lens");
        lens.setAttribute("id", this.lensId || this.imageId + "_lens")
        lens.style.width = this.view.offsetWidth / this.zoomRate + "px"
        lens.style.height = this.view.offsetHeight / this.zoomRate + "px"

        /*insert lens:*/
        this.image.parentElement!!.insertBefore(lens, this.image);
        return lens;
    }

    private moveLens = (e: MouseEvent | TouchEvent) => {
        e.preventDefault();
        let mouseCoords = getMouseCoords(e);
        this.calculateRelativeCursorPosition(mouseCoords)
        this.positionListener(this.position.x, this.position.y)
    };

    private calculateRelativeCursorPosition(mouseCoords: { x: number, y: number }) {
        /*get the x and y positions of the image:*/
        let imgBorderCoordinates: DOMRect = this.image.getBoundingClientRect();
        /*calculate the cursor's x and y coordinates, relative to the image:*/
        let x: number = mouseCoords.x - window.scrollX - imgBorderCoordinates.left;
        let y: number = mouseCoords.y - window.scrollY - imgBorderCoordinates.top;
        x = bound(0, x, this.image.width);
        y = bound(0, y, this.image.height);
        let imgW = imgBorderCoordinates.width;
        let imgh = imgBorderCoordinates.height;
        this.position = {x: x / imgW, y: y / imgh};
    }

    private updateLensPosition = () => {
        let {x, y} = this.position
        let lensX = this.image.offsetWidth * x - this.lens.offsetWidth / 2
        let lensY = this.image.offsetHeight * y - this.lens.offsetHeight / 2
        lensX = bound(0, lensX, this.image.offsetWidth - this.lens.offsetWidth)
        lensY = bound(0, lensY, this.image.offsetHeight - this.lens.offsetHeight)
        this.lens.style.left = lensX + "px";
        this.lens.style.top = lensY + "px";
    };

    private updateLensSize() {
        let lensWidth = this.view.offsetWidth / this.zoomRate;
        let lensHeight = this.view.offsetHeight / this.zoomRate;
        this.lens.style.width = lensWidth + "px"
        this.lens.style.height = lensHeight + "px"
    }

    private updateViewBackgroundPosition = () => {
        let {x, y} = this.position
        let backgroundX = x * this.image.width * this.zoomRate - (this.view.offsetWidth / 2);
        let backgroundY = y * this.image.height * this.zoomRate - (this.view.offsetHeight / 2);
        backgroundX = bound(0, backgroundX, this.image.width * this.zoomRate - (this.view.offsetWidth))
        backgroundY = bound(0, backgroundY, this.image.height * this.zoomRate - (this.view.offsetHeight))
        let backgroundPosition = "-" + backgroundX + "px -" + backgroundY + "px";
        this.view.style.backgroundPosition = backgroundPosition;
    };

    private updateViewBackgroundSize = () => {
        let viewBackgroundWidth = this.image.width * this.zoomRate;
        let viewBackgroundHeight = this.image.height * this.zoomRate;
        this.view.style.backgroundSize = viewBackgroundWidth + "px " + viewBackgroundHeight + "px";
    };

    setPositionPercent = (x: number, y: number) => {
        this.position = {x, y};
        this.updateLensPosition()
        this.updateViewBackgroundPosition()
    };

}

/**
 * Prevents the number value from getting out of bounds.
 * Returns max(lowerBound, min(lowerBound, value))
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

/**
 * Get absolute coords of mouse position for a mouse or touch event
 * @param e the mouse event or touch event
 */
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

