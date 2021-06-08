import {useState , useEffect } from 'react';
import SearchBar from './SearchBar';
import { useHistory , useParams , Link  } from 'react-router-dom'

const SearchPage = () => {
    const {q,page} = useParams();
    const history = useHistory();
    const [searchResults,setSearchResults]=useState([]);
    const [isLoading,setIsLoading] = useState(true);
    const [numResults,setNumResults] = useState(0);

    function goHome(e)
    {
        history.push("/")
    }

    useEffect(()=>
    {
        setIsLoading(true);
        fetch(`http://localhost:8080/search?Search=${q}&Page=${page}`,{
            method : 'GET'
            })
            .then(res=>{
                return res.json()
            })
            .then(data=>{
                var res=[]
                console.log(data.results.length)
                for(let i=0;i<data.results.length;i++)
                {
                    res.push(data.results[i])
                }
                setNumResults(data.total);
                setSearchResults(res)
                setIsLoading(false)
                
            })
    },[q,page])

    return(
        <div className="searchPage">
            <nav className="navbar">
                <div className="title" onClick={(e)=>{goHome(e)}}>BatBatGo</div>
                <SearchBar initialSearch={q} />
            </nav>
           <hr/>
            { isLoading && <div className="loader"></div>}
            { !isLoading &&  (<div className="page">
                    <div className="searchResults">
                        <p className="numberResults">{`About ${numResults} results`}</p><br/><br/>
                    { (searchResults.length>0) &&
                        searchResults.map(result=>
                        (
                            <div className="searchResult" key={result.link}>
                                <p className="link">{result.link}</p><br/>
                                <a href={result.link} target="_blank" rel="noreferrer" className="link">{result.title}</a><br/><br/>
                                <p className="description">{result.description}</p><br/>
                                <br/><br/>
                            </div>
                        ))
                    }
                    {searchResults.length===0 &&  <p className="noMore">No More Results</p> }
                    </div>
                    <div className="navigationLinks">
                        {(page>1) && <Link to={`/search/${q}/${page-1}`} className="navigation">Previous Page</Link> }
                        {(searchResults.length>0) && <Link to={`/search/${q}/${Number(page)+1}`} className="navigation">Next Page</Link>}
                    </div>
                </div>)
            }
        </div>
    )
}

export default SearchPage;