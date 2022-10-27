import React from 'react';
import 'App.css';
import FilesPage from 'pages/FilesPage';
import FileEditPage from "pages/FileEditPage";
import {BrowserRouter, Navigate, Route, Routes} from "react-router-dom";
import Header from "components/Header";
import ProcessesPage from "./pages/ProcessesPage";
import {QueryClient, QueryClientProvider} from "@tanstack/react-query";

const queryClient = new QueryClient()

function App() {
    return (
        <div className='App'>
            <QueryClientProvider client={queryClient}>
                <BrowserRouter>
                    <React.StrictMode>
                        <Header/>
                        <Routes>
                            <Route path='/' element={<Navigate to="/files"/>}/>
                            <Route path="/files/*" element={<FilesPage/>}/>
                            <Route path="/processes/*" element={<ProcessesPage/>}/>
                            <Route path="/edit/*" element={<FileEditPage/>}/>
                        </Routes>
                    </React.StrictMode>
                </BrowserRouter>
            </QueryClientProvider>
        </div>
    );
}

export default App;

