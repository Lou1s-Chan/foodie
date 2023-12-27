import React, { useEffect, useState } from "react";
import styled from "styled-components";

const EventsContainer = styled.div`
  max-height: 225px;
  overflow-y: auto;
  border: none;
  padding: 10px;
  margin: 10px 0;
`;

const EventCard = styled.div`
  background-color: #f0f0f0;
  border-radius: 5px;
  padding: 10px;
  margin-bottom: 10px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
`;
const Restaurant = () => {
  const [orderMessages, setOrderMessages] = useState([]);

  useEffect(() => {
    let eventSource;
    const connectToSSE = () => {
      eventSource = new EventSource("http://localhost:8080/stream");
      eventSource.onmessage = (event) => {
        const data = JSON.parse(event.data);
        console.log("Received message:", data);
        setOrderMessages((prevMessages) => [...prevMessages, data]);
      };

      eventSource.onerror = (error) => {
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
          <EventCard key={index}>{message}</EventCard>
        ))}
      </EventsContainer>
    </div>
  );
};

export default Restaurant;
