import React, { useEffect, useState } from "react";
import styled from "styled-components";

const Restaurant = ({ onStatusChange }) => {
  const [orderMessages, setOrderMessages] = useState([]);

  useEffect(() => {
    let eventSource;
    const connectToSSE = () => {
      eventSource = new EventSource("http://localhost:8080/restaurant_stream");
      eventSource.onopen = () => {
        onStatusChange("connected");
      };
      eventSource.onmessage = (event) => {
        const data = JSON.parse(event.data);
        console.log("Received message:", data);
        setOrderMessages((prevMessages) => [...prevMessages, data]);
      };

      eventSource.onerror = (error) => {
        onStatusChange("disconnected");
        console.error("EventSource failed:", error);
        eventSource.close();
        setTimeout(connectToSSE, 5000); // Try to reconnect after 5 seconds
      };
    };

    connectToSSE();

    return () => {
      if (eventSource) {
        eventSource.close();
      }
    };
  }, []);

  return (
    <div>
      <EventsContainer>
        {orderMessages.map((message, index) => (
          <EventCard key={index} message={message} />
        ))}
      </EventsContainer>
    </div>
  );
};

export default Restaurant;

const Tooltip = styled.div`
  visibility: hidden;
  width: 280px;
  background-color: black;
  color: white;
  text-align: center;
  border-radius: 6px;
  padding: 5px 0;
  position: absolute;
  z-index: 1;

  /* Reposition the tooltip below the EventCard */
  top: 100%; // Changed from bottom: 125%
  left: 50%;
  margin-left: -140px;

  /* Tooltip arrow */
  &::after {
    content: "";
    position: absolute;
    bottom: 100%; // Changed from top: 100%
    left: 50%;
    margin-left: -5px;
    border-width: 5px;
    border-style: solid;
    border-color: transparent transparent black transparent; // Reversed the arrow direction
  }
`;

const MessageCard = styled.div`
  background-color: #f0f0f0;
  border-radius: 5px;
  padding: 10px;
  margin-bottom: 10px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  position: relative;

  &:hover ${Tooltip} {
    visibility: ${(props) => (props.showTooltip ? "visible" : "hidden")};
  }
`;

const EventsContainer = styled.div`
  height: 225px;
  overflow-y: auto;
  border: none;
  padding: 10px;
  margin: 10px 0;
`;

const EventCard = ({ message }) => {
  const isObject = (val) => typeof val === "object" && val !== null;

  const renderCardContent = (message) => {
    if (isObject(message)) {
      return `Order ID: ${message.orderId}, for ${message.restaurantName} (restaurant ID: ${message.restaurantId})`;
    }
    return message;
  };

  const renderOrderDetails = (orderDetails) => {
    return (
      <div style={{ fontSize: "12px" }}>
        {orderDetails.map((item, index) => (
          <div key={index}>
            <strong>{item.foodName}</strong> (ID: {item.foodId})
            <div>
              {" "}
              Price: ${item.price.toFixed(2)} Quantity: {item.quantity}
            </div>
          </div>
        ))}
      </div>
    );
  };

  const renderTooltipContent = (message) => {
    if (typeof message === "object" && message !== null) {
      return (
        <div>
          <div>Customer ID: {message.customerId}</div>
          <hr />
          <div>Restaurant Address: {message.restaurantAddress}</div>
          <hr />
          <div>Order Details:</div>
          {renderOrderDetails(message.orderDetails)}
        </div>
      );
    }
    return message;
  };

  return (
    <MessageCard showTooltip={isObject(message)}>
      {renderCardContent(message)}
      {isObject(message) && <Tooltip>{renderTooltipContent(message)}</Tooltip>}
    </MessageCard>
  );
};
