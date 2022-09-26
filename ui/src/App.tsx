import React from 'react';
import 'App.css';
import FilesPage from 'pages/FilesPage';
import FileEditPage from "pages/FileEditPage";
import {BrowserRouter, Navigate, Route, Routes} from "react-router-dom";

function App() {
  return (
      <div className='App'>
          <BrowserRouter>
              <React.StrictMode>
                  <Routes>
                      <Route path='/' element={ <Navigate to="/files" /> }/>
                      <Route path="/files/*" element={<FilesPage />} />
                      <Route path="/edit/*" element={<FileEditPage />} />
                  </Routes>
              </React.StrictMode>
          </BrowserRouter>
      </div>
  );
}

export default App;

