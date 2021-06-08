import SearchPage from './SearchPage'
import Home from './Home'
import {BrowserRouter as Router, Route, Switch} from 'react-router-dom'

function App() {
  return (
    <Router>
      <div className="App">
          <Switch>
            <Route exact path="/">
              <Home />
            </Route>
            <Route path="/search/:q/:page">
              <SearchPage />
            </Route>
          </Switch>
        </div>
    </Router>
  );
}

export default App;
