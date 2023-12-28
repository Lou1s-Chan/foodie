import "./App.css";
import styled from "styled-components";
import Restaurant from "./Components/RestaurantService/Restaurant";

function App() {
  return (
    <div className="App">
      <DashboardContainer>
        <ServiceCard>
          <h1>User-service</h1>
        </ServiceCard>

        <ServiceCard>
          <h1>Restaurant-service</h1>
          <Restaurant></Restaurant>
        </ServiceCard>

        <ServiceCard>
          <h1>Order-service</h1>
        </ServiceCard>

        <ServiceCard>
          <h1>Payment-service</h1>
        </ServiceCard>

        <ServiceCard>
          <h1>Delivery-service</h1>
        </ServiceCard>

        <ServiceCard></ServiceCard>
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
