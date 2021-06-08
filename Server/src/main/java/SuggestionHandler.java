import javax.servlet.*;
import javax.servlet.*;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class SuggestionHandler extends HttpServlet  {

    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String search = request.getParameter("Search");
        new AutoComplete().postSuggestion(search);
        response.setContentType("application/json");
        response.getWriter().println("{}");

    }

        public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        String search = request.getParameter("Search");
        int max = Integer.parseInt(request.getParameter("Max"));
        response.setContentType("application/json");
        response.getWriter().println(new AutoComplete().getSuggestions(search,max));
    }

}
