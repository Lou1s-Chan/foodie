// import React, { useEffect, useState } from "react";
// import SockJS from "sockjs-client";
// import Stomp from "stompjs";

// const Restaurant = () => {
//   const [orderMessage, setOrderMessage] = useState("");
//   let stompClient = null;

//   const reconnect = () => {
//     setTimeout(() => {
//       connectToWebSocket();
//     }, 5000); // Reconnect after 5 seconds
//   };

//   const connectToWebSocket = () => {
//     const socket = new SockJS("http://localhost:8080/ws");
//     stompClient = Stomp.over(socket);

//     stompClient.connect(
//       {},
//       function (frame) {
//         stompClient.subscribe(
//           "/topic/restaurantService",
//           function (orderMessage) {
//             setOrderMessage(orderMessage.body);
//             console.log(orderMessage.body);
//           }
//         );
//       },
//       function (error) {
//         console.error("Connection error: ", error);
//         reconnect();
//       }
//     );

//     socket.onclose = function () {
//       console.log("WebSocket connection closed");
//       reconnect();
//     };
//   };

//   useEffect(() => {
//     connectToWebSocket();

//     return () => {
//       if (stompClient && stompClient.connected) {
//         stompClient.disconnect();
//       }
//     };
//   }, []);

//   useEffect(() => {
//     console.log({ orderMessage });
//   }, [orderMessage]);

//   return (
//     <div>
//       <h1>OrderMessages</h1>
//       <div>{orderMessage}</div>
//     </div>
//   );
// };

// export default Restaurant;

import React, { useEffect, useState } from "react";

const Restaurant = () => {
  const [orderMessage, setOrderMessage] = useState("");

  useEffect(() => {
    // Create a new EventSource that connects to your SSE endpoint
    const eventSource = new EventSource("http://localhost:8080/stream");

    // Listen for messages
    eventSource.onmessage = (event) => {
      const data = JSON.parse(event.data);
      console.log("Received message:", data);
      setOrderMessage(data);
    };

    // Handle any errors
    eventSource.onerror = (error) => {
      console.error("EventSource failed:", error);
      eventSource.close();
    };

    // Clean up on component unmount
    return () => {
      eventSource.close();
    };
  }, []);

  return (
    <div>
      <h1>Order Messages</h1>
      <div>{orderMessage}</div>
    </div>
  );
};

export default Restaurant;
