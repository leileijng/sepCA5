/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package B_servlets;

import HelperClasses.ShoppingCartLineItem;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 *
 * @author Vanesssa Jiang Lei
 */
@WebServlet(name = "ECommerce_AddFurnitureToListServlet", urlPatterns = {"/ECommerce_AddFurnitureToListServlet"})
public class ECommerce_AddFurnitureToListServlet extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession s = request.getSession();

        String increaseQtyGoodMsg = "";
        String increaseQtyBadMsg = "";
        String badMsg = "";
        String goodMsg = "";

        boolean incart = false;
        boolean good = false;
        try {
            String id = request.getParameter("id");
            String SKU = request.getParameter("SKU");
            double price = Double.parseDouble(request.getParameter("price"));
            String name = request.getParameter("name");
            String imageURL = request.getParameter("imageURL");
            Long countryID = (Long) s.getAttribute("countryID");

            Long storeID = Long.parseLong("10001");
            int itemQty = getQuantity(storeID, SKU);

            //for item available in the stock
            if (itemQty > 0) {
                // Get Cart from Session.
                List<ShoppingCartLineItem> cart = (List<ShoppingCartLineItem>) request.getSession().getAttribute("myCart");
                // If null, create it.
                if (cart == null) {
                    cart = new ArrayList<>();
                    request.getSession().setAttribute("myCart", cart);
                }
                for (ShoppingCartLineItem item : cart) {
                    //for item already in the cart
                    if (item.getSKU().equals(SKU)) {
                        incart = true;
                        //check whether has more stock for this item
                        if (itemQty - item.getQuantity() == 0) {
                            System.out.println("Add quantity failed");
                            increaseQtyBadMsg = "Quantity increase failed due to not enough stock.";
                            //response.sendRedirect("/IS3102_Project-war/B/SG/shoppingCart.jsp?errMsg=" + result);
                        } else {
                            good = true;
                            item.setQuantity(item.getQuantity() + 1);
                            increaseQtyGoodMsg = "Item quantity increased successfully!";
                            //response.sendRedirect("/IS3102_Project-war/B/SG/shoppingCart.jsp?goodMsg=" + result);
                        }
                    }
                    break;
                }

                //if the item is newly added into the shopping cart
                if (incart == false) {
                    ShoppingCartLineItem cartItem = new ShoppingCartLineItem();
                    cartItem.setId(id);
                    cartItem.setSKU(SKU);
                    cartItem.setPrice(price);
                    cartItem.setName(name);
                    cartItem.setQuantity(1);
                    cartItem.setCountryID(countryID);
                    cartItem.setImageURL(imageURL);
                    cart.add(cartItem);
                    good = true;
                    goodMsg = "Item successfully added into the cart!";
                }
                //response.sendRedirect("/IS3102_Project-war/B/SG/shoppingCart.jsp?goodMsg=" + result);
            } //not enough stock
            else {
                good = false;
                badMsg = "Item not added to cart, not enough quantity available.";
                //response.sendRedirect("/IS3102_Project-war/B/SG/shoppingCart.jsp?errMsg=" + result);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        if (good) {
            if (incart) {
                response.sendRedirect("/IS3102_Project-war/B/SG/shoppingCart.jsp?goodMsg=" + increaseQtyGoodMsg);
            } else {
                response.sendRedirect("/IS3102_Project-war/B/SG/shoppingCart.jsp?goodMsg=" + goodMsg);
            }
        } else if (incart && !good) {
            response.sendRedirect("/IS3102_Project-war/B/SG/shoppingCart.jsp?errMsg=" + increaseQtyBadMsg);
        } else {
            response.sendRedirect("/IS3102_Project-war/B/SG/shoppingCart.jsp?errMsg=" + badMsg);
        }
    }

    public int getQuantity(Long storeID, String SKU) {
        try {
            System.out.println("getQuantity() SKU: " + SKU);
            Client client = ClientBuilder.newClient();
            WebTarget target = client
                    .target("http://localhost:8080/IS3102_WebService-Student/webresources/entity.storeentity")
                    .path("getQuantity")
                    .queryParam("storeID", storeID)
                    .queryParam("SKU", SKU);
            Invocation.Builder invocationBuilder = target.request(MediaType.APPLICATION_JSON);
            Response response = invocationBuilder.get();
            System.out.println("status: " + response.getStatus());
            if (response.getStatus() != 200) {
                return 0;
            }
            String result = (String) response.readEntity(String.class);
            System.out.println("Result returned from ws: " + result);
            return Integer.parseInt(result);

        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }

    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
