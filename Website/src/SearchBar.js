import {useState , useEffect } from 'react';
import {useHistory} from 'react-router-dom'

const SearchBar=({initialSearch})=>{

    const [search,setSearch] = useState(initialSearch);
    const [suggestions,setSuggestions] = useState([]);
    const history = useHistory();
    async function handleSearch(event)
    {
        event.preventDefault();
        fetch(`http://localhost:8080/suggest?Search=${search}`,{
            method : 'POST'
            })
        history.push(`/search/${search}/1`);
    }

    async function handleChange(event)
    {
        setSearch(event.target.value)
        fetch(`http://localhost:8080/suggest?Search=${event.target.value}&Max=${5}`,{
            method : 'GET'
            })
            .then(res=>{
                return res.json()
            })
            .then(data=>{
                var sugArray=[]
                for(let i=0;i<data.suggestions.length;i++)
                {
                    sugArray.push(data.suggestions[i].suggestion)
                }
                setSuggestions(sugArray) 
            })
    }

    useEffect(()=>
    {

    },[search])


    return (
        <div className="SearchBar">
            <form onSubmit={(e)=>handleSearch (e)} className="SearchBarForm">
                <input 
                    type="text" 
                    className="Searchbar"
                    required
                    onChange={(e)=>{handleChange(e)}}
                    value={search}
                    list = "suggestionList"
                    autoComplete="off"
                 />
                 <div className="hideCursor"></div>

                <button>Search</button>
                <datalist className="suggestionList" id="suggestionList">
                {
                    suggestions.map(suggestion=>(
                        <option key={suggestion} value={suggestion} className="suggestion"/>
                    ))
                }
                </datalist>

                
            </form>
        </div>
    )
}
export default SearchBar;