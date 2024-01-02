import "./App.css";
import styled from "styled-components";
import Restaurant from "./Components/RestaurantService/Restaurant";
import Order from "./Components/OrderService/Order";
import User from "./Components/UserService/User";
import Delivery from "./Components/DeliveryService/Delivery";
import Payment from "./Components/PaymentService/Payment";
import { useState } from "react";

function App() {
  const [paymentStatus, setPaymentStatus] = useState("offline");
  const [deliveryStatus, setDeliveryStatus] = useState("offline");
  const [orderStatus, setOrderStatus] = useState("offline");
  const [userStatus, setUserStatus] = useState("offline");
  const [restaurantStatus, setRestaurantStatus] = useState("offline");

  const handlePaymentStatusChange = (status) => {
    setPaymentStatus(status);
  };
  const handleDeliveryStatusChange = (status) => {
    setDeliveryStatus(status);
  };
  const handleOrderStatusChange = (status) => {
    setOrderStatus(status);
  };
  const handleUserStatusChange = (status) => {
    setUserStatus(status);
  };
  const handleRestaurantStatusChange = (status) => {
    setRestaurantStatus(status);
  };

  const paymentStatusStyle = {
    color: paymentStatus === "online" ? "green" : "red",
  };
  const userStatusStyle = {
    color: userStatus === "online" ? "green" : "red",
  };
  const restaurantStatusStyle = {
    color: restaurantStatus === "online" ? "green" : "red",
  };
  const orderStatusStyle = {
    color: orderStatus === "online" ? "green" : "red",
  };
  const deliveryStatusStyle = {
    color: deliveryStatus === "online" ? "green" : "red",
  };

  return (
    <div className="App">
      <DashboardContainer>
        <ServiceCard>
          <h1>User-service</h1>
          <User onStatusChange={handleUserStatusChange} />
        </ServiceCard>

        <ServiceCard>
          <h1>Restaurant-service</h1>
          <Restaurant onStatusChange={handleRestaurantStatusChange} />
        </ServiceCard>

        <ServiceCard>
          <h1>Order-service</h1>
          <Order onStatusChange={handleOrderStatusChange} />
        </ServiceCard>

        <ServiceCard>
          <h1>Payment-service</h1>
          <Payment onStatusChange={handlePaymentStatusChange} />
        </ServiceCard>

        <ServiceCard>
          <h1>Delivery-service</h1>
          <Delivery onStatusChange={handleDeliveryStatusChange} />
        </ServiceCard>

        <ServiceCard>
          <h1>Foodies System Status</h1>
          <br />
          <p style={userStatusStyle}>User Service is: {userStatus}</p>
          <br />
          <p style={restaurantStatusStyle}>
            Restaurant Service is: {restaurantStatus}
          </p>
          <br />
          <p style={orderStatusStyle}>Order Service is: {orderStatus}</p>
          <br />
          <p style={paymentStatusStyle}>Payment Service is: {paymentStatus}</p>
          <br />
          <p style={deliveryStatusStyle}>
            Delivery Service is: {deliveryStatus}
          </p>
        </ServiceCard>
      </DashboardContainer>
    </div>
  );
}

export default App;

const DashboardContainer = styled.div`
  height: 100vh;
  width: 100%;
  background-color: white;
  display: flex;
  flex-direction: row;
  align-items: center;
  align-content: space-around;
  justify-content: space-around;
  flex-wrap: wrap;
`;

const ServiceCard = styled.div`
  height: 325px;
  width: 455px;
  background-color: white;
  display: flex;
  flex-direction: column;
  color: black;
  border-radius: 10px;
  padding: 20px;
  box-shadow: 2px 2px 8px rgba(0, 0, 0, 0.2);
`;
