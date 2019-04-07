import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import utils.GetInfo;
import utils.PostInfo;
import utils.SqliteHelper;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;


@WebServlet(name = "RelationSubmitServlet", urlPatterns = "")
public class RelationSubmitServlet extends HttpServlet {
    public static final int MAX_RECORD_ID = 99709 + 1;
    private Gson gson = new Gson();
    private static int ID_LIMIT = 0;
    private Connection connection;

    @Override
    public void init() throws ServletException {
        super.init();
        try {
            connection = SqliteHelper.getConnection();
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        Statement stmt = null;
        try {
            stmt = connection.createStatement();
            BufferedReader br = new BufferedReader(new InputStreamReader((ServletInputStream) request.getInputStream(),
                    "utf-8"));
            StringBuilder stringBuilder = new StringBuilder();
            String info;
            while ((info = br.readLine()) != null) {
                stringBuilder.append(info);
            }
            br.close();
            info = stringBuilder.toString();
//            System.out.println(info);
            Type listType = new TypeToken<ArrayList<PostInfo>>() {
            }.getType();
            ArrayList<PostInfo> infoList = new Gson().fromJson(info, listType);

            for (PostInfo anInfoList : infoList) {
                String sql = String.format("UPDATE Data SET relation=%d WHERE id=%d", anInfoList.relation, anInfoList.id);
                System.out.println(sql);
                stmt.execute(sql);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            response.setContentType("text/html; charset=UTF-8");
            response.setCharacterEncoding("utf-8");
            response.getWriter().write(e.toString());
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        Statement stmt = null;
        try {
            stmt = connection.createStatement();
            String num = request.getParameter("number");
            String json = "";
            if (num == null) {
                json = "{}";
            } else {
                // String sql = "SELECT * FROM Data WHERE relation = 0 AND id > " + String.valueOf(ID_LIMIT) + " LIMIT " + num;
                String sql = String.format("SELECT * from Data WHERE relation=0 AND id>%d LIMIT %s", ID_LIMIT, num);
                ID_LIMIT += Integer.parseInt(num) * 10 % MAX_RECORD_ID;// magic value: 避免返回重复
//                System.out.println(sql);
                ResultSet resultSet = stmt.executeQuery(sql);
                Map<String, ArrayList<GetInfo>> data = new LinkedHashMap<>();
                ArrayList<GetInfo> infos = new ArrayList<GetInfo>();
                while (resultSet.next()) {
                    GetInfo p_info = new GetInfo(resultSet.getInt("id"), resultSet.getString("entity_a"),
                            resultSet.getString("entity_b"), resultSet.getString("sentence"));
                    infos.add(p_info);
                }
                data.put("info", infos);
                Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();
                json = gson.toJson(data);
            }
            response.setContentType("application/json");
            response.setCharacterEncoding("utf-8");
            response.getWriter().write(json);
        } catch (SQLException e) {
            e.printStackTrace();
            response.setContentType("text/html; charset=UTF-8");
            response.setCharacterEncoding("utf-8");
            response.getWriter().write(e.toString());
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

    }
}
