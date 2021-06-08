import SearchBar from './SearchBar';
const Home= () => {
    return(
        <div className="Home">
            <div className="Searcher">
                <div className="title">
                    BatBatGo
                </div>
                <SearchBar initialSearch={""}/>
            </div>
        </div>
    )
}
export default Home