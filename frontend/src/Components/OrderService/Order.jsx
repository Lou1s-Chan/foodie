import React, { useEffect, useState } from "react";
import styled from "styled-components";

const Order = ({ onStatusChange }) => {
  const [orderMessages, setOrderMessages] = useState([]);

  useEffect(() => {
    let eventSource;
    const connectToSSE = () => {
      eventSource = new EventSource("http://localhost:8081/order_stream");
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

export default Order;

const MessageCard = styled.div`
  background-color: #f0f0f0;
  border-radius: 5px;
  padding: 10px;
  margin-bottom: 10px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  position: relative;
`;

const EventsContainer = styled.div`
  height: 225px;
  overflow-y: auto;
  border: none;
  padding: 10px;
  margin: 10px 0;
`;

const EventCard = ({ message }) => {
  const renderCardContent = (message) => {
    return message;
  };
  return <MessageCard>{renderCardContent(message)}</MessageCard>;
};
