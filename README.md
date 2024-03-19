# CodeViz

## Setup

To clone the repo (note that if the default folder name "CodeViz" is used, IntelliJ may not recognize `codeviz_frontend` as a module properly):
```git clone https://github.com/maishabd23/CodeViz.git CodeVizFolder```

## Running the Application in Development Mode (locally)

To start up the backend, run the main method in the `App` class in the `codeViz` package.\
This should run the app on [http://localhost:8080](http://localhost:8080).

Navigate to the frontend directory (should be similar to `CodeViz\codeviz_frontend`) on the terminal, and run the following:

### `npm start`

This will run the frontend of the app in development mode.\
Open [http://localhost:3000](http://localhost:3000) to view it in your browser.

If you get an error, you may need to first [install node.js](https://nodejs.org/en/download) and run the following command on the terminal:

```
cd C:\Desktop\UNI_LESSON_FILES\Year5\Capstone\CodeViz\codeviz_frontend
rmdir /s /q node_modules
npm install
```
#### `npm install`
#### `npm install graphology-gexf graphology sigma graphology-layout-forceatlas2`

