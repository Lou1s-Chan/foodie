import React, { useEffect, useState } from "react";

const Restaurant = () => {
  const [orderMessage, setOrderMessage] = useState("");

  useEffect(() => {
    let eventSource;
    const connectToSSE = () => {
      eventSource = new EventSource("http://localhost:8080/stream");
      eventSource.onmessage = (event) => {
        const data = JSON.parse(event.data);
        console.log("Received message:", data);
        setOrderMessage(data);
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
      <div>{JSON.stringify(orderMessage)}</div>
    </div>
  );
};

export default Restaurant;
