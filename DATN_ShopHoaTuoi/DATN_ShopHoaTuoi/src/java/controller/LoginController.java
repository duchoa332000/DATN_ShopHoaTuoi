/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controller;

import entity.Gender;
import entity.Staff;
import entity.User;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpSession;
import org.hibernate.Query;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import validate.ValidateUser;

@Transactional
@Controller
public class LoginController {

    @Autowired
    SessionFactory factory;

    @RequestMapping("login")
    public String login() {

        return "login";
    }

    @RequestMapping(value = "login", method = RequestMethod.POST)
    public String login(ModelMap model,
            @RequestParam("id") String id,
            @RequestParam("password") String password,
            HttpSession httpsession) {
        Session session = factory.getCurrentSession();
        if (isNumeric(id)) {
            try {
                String hql = "FROM Staff where phone='" + id + "'";
                Query query = session.createQuery(hql);
                Staff staff = (Staff) query.list().get(0);
                if (staff.getPassword().equals(password)) {
                    httpsession.setAttribute("staff", staff);
                    httpsession.setAttribute("idstaff", staff.getId());
                    httpsession.setAttribute("rolestaff", staff.getRole());
                    return "redirect:/admin/dashboardmanage";
                } else if(staff.getPassword().trim().length()==0 || staff.getPhone().trim().length()==0){
                    model.addAttribute("message", "Vui l??ng nh???p t??i kho???n v?? m???t kh???u!");
                } else {
                    model.addAttribute("message", "Sai t??i kho???n ho???c m???t kh???u!");
                }
            } catch (Exception e) {
                System.out.println(e);
                model.addAttribute("message", "T??i kho???n kh??ng t???n t???i!");
            }
        } else {
            try {
                String hql = "FROM User where email='" + id + "'";
                Query query = session.createQuery(hql);
                User user = (User) query.list().get(0);
                if(user.getIsactive().getId() == 2){
                    model.addAttribute("message", "T??i kho???n c???a b???n ???? b??? kh??a!");
                } else if (user.getPassword().equals(password)) {
                    httpsession.setAttribute("user", user);
                    httpsession.setAttribute("iduser", user.getId());
                    return "redirect:/home";
                } else if(user.getPassword().trim().length()==0 || user.getEmail().trim().length()==0){
                    model.addAttribute("message", "Vui l??ng nh???p t??i kho???n v?? m???t kh???u!");
                } else {
                    model.addAttribute("message", "Sai t??i kho???n ho???c m???t kh???u!");
                }
            } catch (Exception e) {
                System.out.println(e);
                model.addAttribute("message", "T??i kho???n kh??ng t???n t???i!");
            }
        }

        return "login";
    }

    @RequestMapping("register")
    public String register(ModelMap model) {
        model.addAttribute("user", new User());
        model.addAttribute("users", getUsers());
        return "register";
    }

    @RequestMapping(value = "reg", method = RequestMethod.POST)
    public String reg(ModelMap model, @ModelAttribute("user") User user, @RequestParam("repassword")String repassword) {
        Session session = factory.openSession();
        Transaction t = session.beginTransaction();
        if(!user.getEmail().matches("^[a-zA-Z][\\w-]+@([\\w]+\\.[\\w]+|[\\w]+\\.[\\w]{2,}\\.[\\w]{2,})$")){
            model.addAttribute("message", "Sai ?????nh d???ng email!");
        } else if (!vaidateReg(user.getEmail()).isEmpty()) {
            model.addAttribute("message", "Email n??y ???? c?? ng?????i s??? d???ng!");
        } else if (!user.getPhone().matches("[0-9]{10}")){
            model.addAttribute("message", "S??? ??i???n tho???i ph???i nh???p 10 ch??? s???!");
        } else if(!vaidateRegPhone(user.getPhone()).isEmpty()){
            model.addAttribute("message", "S??? ??i???n tho???i ???? t???n t???i!");
        } else if(user.getPassword().trim().length() < 6 ){
            model.addAttribute("message", "M???t kh???u ??t nh???t ph???i l?? 6 ch??? s???!");
        } else if(!user.getPassword().equals(repassword)){
            model.addAttribute("message", "X??c nh???n m???t kh???u kh??ng ch??nh x??c!");
        } else {
            try {
                user.setAvatar("user-image.jpg");
                session.save(user);
                t.commit();
                model.addAttribute("message", "????ng K?? Th??nh C??ng!");
                return "redirect:/login";
            } catch (Exception e) {
                t.rollback();
                e.printStackTrace();
                model.addAttribute("message", "Th??m m???i th???t b???i!");
            } finally {
                session.close();
            }
        }
        return "register";
    }

    public static boolean isNumeric(String str) {
        NumberFormat formatter = NumberFormat.getInstance();
        ParsePosition pos = new ParsePosition(0);
        formatter.parse(str, pos);
        return str.length() == pos.getIndex();
    }

    @RequestMapping("logoff")
    public String logoff(HttpSession httpSession) {
        httpSession.removeAttribute("user");
        httpSession.removeAttribute("staff");
        return "redirect:/login";
    }

    @ModelAttribute("genders")
    @SuppressWarnings("unchecked")
    public List<Gender> getGenders() {
        Session session = factory.getCurrentSession();
        String hql = "FROM Gender";
        Query query = session.createQuery(hql);
        List<Gender> list = query.list();
        return list;
    }

    @SuppressWarnings("unchecked")
    public List<User> getUsers() {
        Session session = factory.getCurrentSession();
        String hql = "FROM User";
        Query query = session.createQuery(hql);
        List<User> list = query.list();
        return list;
    }
    
    /////////////////////Validate///////////////////////

    @SuppressWarnings("unchecked")
    public List<User> vaidateReg(String email) {
        Session session = factory.getCurrentSession();
        String hql = "FROM User where email = :email";
        
        Query query = session.createQuery(hql);
        query.setParameter("email", email);
        List<User> list = query.list();
        return list;
    }
    
    @SuppressWarnings("unchecked")
    public List<User> vaidateRegPhone(String phone) {
        Session session = factory.getCurrentSession();
        String hql = "FROM User where phone = :phone";
        
        Query query = session.createQuery(hql);
        query.setParameter("phone", phone);
        List<User> list = query.list();
        return list;
    }
}
