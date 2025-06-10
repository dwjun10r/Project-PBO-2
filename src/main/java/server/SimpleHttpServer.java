package server;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.net.URI;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.format.DateTimeParseException;


import config.DbConnection;
import dao.VillaDAO;
import dao.RoomTypeDAO;
import dao.CustomerDAO;
import dao.BookingDAO;
import dao.ReviewDAO;
import dao.VoucherDAO;
import models.Villa;
import models.RoomType;
import models.Customer;
import models.Booking;
import models.Review;
import models.Voucher;

public class SimpleHttpServer {


}