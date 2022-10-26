import {Link} from "react-router-dom";
import React from "react";
import "styles/Header.css"

function Header() {
    return <>
        <nav className={"header"}>
            <div className={"header_logo"}>
                <span className={"header_logo_text"}>Media</span>
            </div>
            <NavItem to={"/"} text={"Home"}/>
            <NavItem to={"/files"} text={"Files"}/>
            <NavItem to={"/processes"} text={"Processes"}/>
        </nav>
    </>
}

function NavItem(props: {
    to: string,
    text: string
}) {
    return <Link
        to={props.to}
        className={"header_nav-item"}
    >
        {props.text}
    </Link>
}

export default Header
