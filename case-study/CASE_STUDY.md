# Case Study Scenarios to discuss

## Scenario 1: Cost Allocation and Tracking
**Situation**: The company needs to track and allocate costs accurately across different Warehouses and Stores. The costs include labor, inventory, transportation, and overhead expenses.

**Task**: Discuss the challenges in accurately tracking and allocating costs in a fulfillment environment. Think about what are important considerations for this, what are previous experiences that you have you could related to this problem and elaborate some questions and considerations

**Questions you may have and considerations:**
```txt
Questions:
 - How does the company currently track costs across different Warehouses and Stores? Are there any existing systems or tools in place for cost tracking? 
 - In case of item return, which warehouse will be responsible for the cost of return processing?
 - How will we handle shared resources between warehouses, such as transportation or labor costs? 
 - How will we handle international shipping costs and customs duties for cross-border fulfillment?
 - In wich currency will we track costs, and how will we handle currency conversion for international operations?
 - In case of a warehouse closure or relocation, how will we allocate costs for the transition period? (archive warehouse)
 - If a customer order is split and shipped from Warehouse A and Store B, does the originating business unit absorb both shipping costs, or are they split by node?
 - If inventory sits in a warehouse for 6 months without moving, does that holding cost get billed back to the buying team (business unit) or absorbed by the warehouse facility?
 - Is cost allocated at the moment of inventory receipt, at the point of digital order capture, or upon physical shipment confirmation?

Considerations:
 - Granularity of Data: The system must track costs at the SKU-Location-Transaction level. Aggregated daily or weekly logs destroy visibility into true fulfillment costs.
 - Standard vs. Actual Costing: Decide if you will allocate costs using predetermined standards (e.g., an estimated $1.50 per pick) and reconcile variances monthly, or if you will calculate actual fluctuating costs in real time.
 - Reporting and Analytics: The system should provide dashboards and reports that allow managers to see cost trends, identify inefficiencies, and make data-driven decisions. For system design it is crucial to know business needs early. The design also should be flexible for futre changes in business rules.
 - Dynamic Rule Engine: Cost allocation rules change. A business unit code may split transport costs 60/40 between two warehouses this quarter, but shift to 50/50 next quarter. The logic must be driven by configurable metadata, not hardcoded into services.
```

## Scenario 2: Cost Optimization Strategies
**Situation**: The company wants to identify and implement cost optimization strategies for its fulfillment operations. The goal is to reduce overall costs without compromising service quality.

**Task**: Discuss potential cost optimization strategies for fulfillment operations and expected outcomes from that. How would you identify, prioritize and implement these strategies?

**Questions you may have and considerations:**
```txt
Questions:
 - What are the current fulfillment costs broken down by category (labor, transportation, inventory holding, packaging, etc.)? 
 - Are there any existing bottlenecks or inefficiencies in the fulfillment process that can be addressed to reduce costs?
 - How do we measure service quality and customer satisfaction to ensure that cost optimization does not negatively impact the customer experience?
 - Are there any industry benchmarks or best practices that we can leverage for cost optimization in fulfillment operations?
 - How will we track and measure the success of implemented cost optimization strategies?
 - Are carrier contracts fixed or volume-tiered?
 - How real-time and accurate is our inventory visibility?
 - What external APIs do we need to orchestrate? Do we need real-time rate shopping calls to carrier APIs (e.g., FedEx, UPS, DHL) during the checkout/routing phase, or will we work with cached shipping rate tables?


Strategies & Expected Outcomes
------------------------------
Dynamic Order Routing (Distributed Order Management - DOM):
 - System uses real-time data to route orders to the closest node (warehouse or store) with available stock.
 - Drastically reduces last-mile transit costs.
 - Minimizes split shipments.
 - Lowers delivery times.

Zone Skipping & Carrier Diversification: (personal experience that items can be arrive one by one if they are in different zones)
 - Aggregating packages heading to the same distant region, trucking them in bulk to a local carrier hub, and injecting them directly into the local last-mile network.
 - Bypasses expensive carrier zones.
 - Lowers per-package shipping rates by 15–30%.
 - Reduces reliance on a single carrier monopoly.
 
Automated Inventory Wave Rebalancing:
 - Using predictive analytics to cross-dock or shift inventory from slow-moving nodes to high-velocity nodes before demand peaks.
 - Minimizes emergency expedited shipping costs.
 - Reduces safety stock overhead costs.
 - Maximizes warehouse turn rates. 

Optimized Box Selection (Cube Utilization): (personal experience that packages sometimes overpacked)
 - Algorithmic packaging selection based on dimensional SKU data, ensuring the smallest possible box is utilized for an order.
 - Reduces dimensional weight (DIM) carrier surcharges.
 - Lowers packaging material costs.
 - Maximizes space inside carrier trailers.
 
Implementation and Techical Considerations:
-------------------------------------------
Iterative Rollout: (what we did at chess.com)
 - A/B Pilot Testing: Do not launch a routing algorithm globally overnight. Roll out the new optimization logic to a single warehouse or regional cluster first.
 - Shadow Mode Execution: Run the new optimization service in "Shadow Mode" in production. Have the system calculate the optimized routes silently in the logs, and compare them against actual manual decisions to calculate theoretical savings before going live.

Leveraging Data Science & Machine Learning:
 - Use historical order, inventory, and shipping data to train predictive models that forecast demand spikes, identify slow-moving SKUs, and recommend optimal inventory placements.
 - Continuously refine models based on real-world performance metrics.
 - Use AI to identify patterns in returns, cancellations, and customer complaints to proactively adjust fulfillment strategies.

Data-Driven Discovery:
 - Cost-per-Order Baseline: Audit existing data to establish a granular cost-per-order benchmark across different nodes (e.g., Warehouse A fulfillment costs $4.50/order vs. Store B micro-fulfillment costs $7.20/order).
 - Variance Analysis: Pinpoint anomalies. Why does one warehouse have a 20% higher packaging cost than another? Why are shipping zones 5–8 being utilized frequently for local orders?

Design Patterns for Flexibility:
 - Decoupled Strategy Pattern: The algorithm used to determine the "cheapest fulfillment node" will change frequently based on fuel prices, carrier contract updates, or seasonal labor costs. The Strategy Pattern usage in service layer allows easily swap routing logic without refactoring main workflows.
 - Event-Driven Design and Telemetry: Publish telemetry events (e.g., OrderRoutedEvent, PackageDimensionCalculatedEvent) to Kafka or RabbitMQ so data analysts can monitor variance in real time.
```

## Scenario 3: Integration with Financial Systems
**Situation**: The Cost Control Tool needs to integrate with existing financial systems to ensure accurate and timely cost data. The integration should support real-time data synchronization and reporting.

**Task**: Discuss the importance of integrating the Cost Control Tool with financial systems. What benefits the company would have from that and how would you ensure seamless integration and data synchronization?

**Questions you may have and considerations:**
```txt
Integrating an operational cost tool with enterprise financial systems (like SAP, Oracle, or NetSuite) bridges the gap between physical supply chain logistics and corporate accounting.

Core Benefits of Integration:
-----------------------------
 - Unified Source of Truth: Eliminates data silos and costly discrepancies between operational tracking (e.g., units picked/shipped) and ledger accounting (e.g., dollars spent/invoiced).
 - Automated Accruals & Real-Time Closing: Financial teams can view accrued costs in real time instead of waiting until the end of the month to compile carrier invoices and labor logs, drastically reducing month-end closing cycles.
 - Granular Margin Visibility: Allows the company to calculate the true net profitability of individual orders, specific product categories, or regional distribution channels by blending live fulfillment expenses into corporate revenue data.
 - Proactive Variance Detection: Enables immediate flagging of financial leaks—such as sudden fuel surcharge spikes or runaway store-fulfillment labor costs—before they drain seasonal profit margins.

Ensuring Seamless Integration & Data Synchronization
----------------------------------------------------
 - Asynchronous Streaming: Dont use synchronous REST calls during live operations. In case of downtime or a slow response window, workflows will block or crash. Message brokers like Apache Kafka or RabbitMQ to can streams cost events (e.g., FreightCostCalculated, LaborBatchLogged) asynchronously.
 - Event streeming: Save all cost events in a durable event log, allowing financial systems to replay or backfill data if they fall behind or experience downtime.
 - Dead Letter Queues (DLQ): Implement DLQs to capture failed cost events for later inspection and reprocessing, ensuring no data is lost during transient failures.
 - Command Query Responsibility Segregation (CQRS): Separate the read and write models for cost data. Operational systems can write cost events to a log, while financial systems can query a read-optimized view of aggregated costs without impacting operational performance.
 - Idempotency Safeguards: in case of financial data analitics, ensure that cost events are processed exactly once, even if they are sent multiple times due to network retries or system failures.
 - Data precalculation and caching: Pre-aggregate cost data for common queries (e.g., total labor cost per warehouse per day) to reduce load on financial systems and improve response times.

```



## Scenario 4: Budgeting and Forecasting
**Situation**: The company needs to develop budgeting and forecasting capabilities for its fulfillment operations. The goal is to predict future costs and allocate resources effectively.

**Task**: Discuss the importance of budgeting and forecasting in fulfillment operations and what would you take into account designing a system to support accurate budgeting and forecasting?

**Questions you may have and considerations:**
```txt
Past predicts the future but is no guarantee of results.
Budgeting and forecasting shift the fulfillment operation from a reactive posture to a predictive one. 
In supply chain logistics, an inaccurate forecast directly manifests as either severe understaffing during peak seasons (causing missed delivery SLAs) or bloated inventory holding costs (draining corporate capital).

Importance of Budgeting and Forecasting in Fulfillment
------------------------------------------------------
 - Labor Resource Capacity Planning: Fulfillment is a labor-heavy operation. Accurate forecasts tell facility managers weeks or months in advance exactly when to hire temporary seasonal workers, schedule overtime, or scale down shifts.
 - Carrier Volume Commitments: Most enterprise logistics networks sign volume-tiered contracts with national carriers (UPS, FedEx, DHL). Predictive data ensures the company can hit contract minimums to protect their discounted tier rates and accurately secure trailer space before peak holiday capacity limits kick in.
 - Infrastructure & Expansion Strategy: Predicting long-term growth trends allows executives to decide when a warehouse is nearing maximum throughput capacity. This provides a multi-month runway to lease new micro-fulfillment spaces or renegotiate regional third-party logistics (3PL) contracts.

Crucial Variables
-----------------
Macro and Micro Volume Drivers
 - Historical Seasonality & Growth Rates: Ingesting multi-year transaction data to capture cyclical trends (e.g., Black Friday/Cyber Monday spikes, summer lulls) combined with an overlay of planned corporate year-over-year growth targets.
 - Marketing & Merchandising Pipeline: The system cannot rely on historical data alone. If the marketing team plans a massive flash-sale campaign next month for bulky, high-volume SKUs, the system must translate that marketing unit forecast into an operational volumetric storage and labor-hour forecast.

Fluid Cost Variables:
 - Variable Tariffs & Surcharges: Carrier fuel surcharges, residential delivery fees, and peak-season regional surcharges change constantly. The forecasting engine must apply dynamic, time-bound financial rate tables rather than static cost assumptions.
 - Geographic Shifts: Analyzing changing customer demographics. If historical data shows customer clusters in Zone 2, but recent orders are trending heavily toward Zone 6, the system must forecast higher baseline transit and shipping costs despite identical order volumes.


System Architecture Considerations
----------------------------------
 - Separation of Operational and Analytical Processing (CQRS): Running complex, multi-year forecasting models involves heavy data aggregation that can easily cripple an operational transactional database. Use Command Query Responsibility Segregation (CQRS). Keep your core Quarkus fulfillment services pure, and stream data out to an analytical platform (like Snowflake, BigQuery, or a dedicated PostgreSQL read-replica) for simulation runs.
 - Data Lake & Historical Archive: A centralized repository for all historical fulfillment data (orders, shipments, labor logs, inventory levels) to feed predictive models and allow for backtesting of forecast accuracy.
 - Machine Learning Forecasting Models: Implement time-series forecasting algorithms (e.g., ARIMA, Prophet, LSTM neural networks) to predict future order volumes, labor needs, and shipping costs based on historical patterns and external variables.
 - Scenario Simulation & What-If Analysis: Allow managers to simulate different scenarios (e.g., sudden 20% increase in order volume, new marketing campaign) to see how it impacts labor, inventory, and shipping costs before committing resources.
 - Continuous Feedback Loop: Integrate real-time operational data back into the forecasting model to continuously refine predictions and improve accuracy over time.

Questions & Considerations
--------------------------
Strategic Business Questions:
 - What is the required variance tolerance (Accuracy SLA)? Is a ±5% budget variance acceptable at the executive level, or does the business require a rolling weekly re-forecast that matches actual expenditures within 2%?
 - How should we handle unexpected disruptions in our baseline forecast? If a major global event or carrier strike completely skews historical data for a 3-month window, how do analysts flag and exclude those historic anomalies so they don’t poison future predictive models?

Technical & Data Questions:
 - How frequently must the forecast be re-calculated? Is the budget generated once a year and re-forecasted quarterly, or do we need a dynamic, rolling engine that updates a 30-day operational outlook every night based on the day's real-time checkout volume? 
 - What is the lowest level of forecasting granularity required? Do we forecast aggregated total fulfillment spend at the corporate level, or must the system project costs down to individual businessUnitCode allocations per warehouse node?To wrap up our discussion on this case study framework, let me know:Do you have a Scenario 5 to map out, or would you like to compile these 4 scenarios into a comprehensive summary structure?Would you like to explore how to design the Quarkus service interface for handling the "What-If" scenario execution engine?AI responses may include mistakes. Learn moreSlimstockCapacity Planning: What Is, Strategies & Best Practices10 Jul 2025 — What challenges may arise during the capacity planning process? Challenges can arise during the capacity planning process, such as...Kyvos InsightsWhat-If Analysis with Kyvos Viz18 Jul 2024 — How Does What-If Analytics Work It's a given: forecasting business scenarios isn't a piece of cake. Predicting a clean pattern or ...The Access GroupFinancial Forecasting Explained for Growing BusinessesThey ( a business ) may also decide to run a quarterly forecasting exercise which takes the budget as its base case and then makes...
```

## Scenario 5: Cost Control in Warehouse Replacement
**Situation**: The company is planning to replace an existing Warehouse with a new one. The new Warehouse will reuse the Business Unit Code of the old Warehouse. The old Warehouse will be archived, but its cost history must be preserved.

**Task**: Discuss the cost control aspects of replacing a Warehouse. Why is it important to preserve cost history and how this relates to keeping the new Warehouse operation within budget?

**Questions you may have and considerations:**
```txt
Importance of Preserving Cost History
-------------------------------------
 - Establishing a Cost Baseline: You cannot prove a new multi-million dollar warehouse is "more efficient" if you delete the benchmark data of the facility it replaced. Preserving cost history provides the historical Cost-Per-Order (CPO) and labor metrics required to measure the true return on investment (ROI) of the physical transition.
 - Preventing Financial Statement Distortion: Reusing the same businessUnitCode means corporate finance expects a continuous ledger. If historical transaction costs are detached or wiped out, longitudinal financial auditing, year-over-year (YoY) variance reporting, and tax deprecation calculations break down completely.
 - Predictive Maintenance and Variable Seasonality: Warehouse operational costs are highly cyclical. Retaining historical data ensures that predictable trends (such as seasonal utility spikes or localized holiday labor demands) remain visible, preventing the new facility from treating predictable historical fluctuations as unexpected budget overruns.
 - Isolating Transition Overlaps (The "Double Rent" Trap): During a warehouse replacement, costs spike significantly due to overlapping leases, redundant skeleton crews working both locations, and freight relocation fees. Access to historical operational margins allows the system to isolate these temporary transition anomalies from the permanent operating budget of the new facility.
 - Calibrating Automated Guardrails: If your application enforces automated cost thresholds or fraud limits (e.g., auto-flagging warehouse invoices that exceed the normal range by 15%), the system needs historical data to train its baseline validation logic. Lacking this context causes the tool to either trigger endless false positives or fail to detect genuine supplier overcharges.

System Architecture Considerations
----------------------------------
 - Temporal or Slow-Moving Dimension Mapping: Because the businessUnitCode is being recycled, querying the system simply by code will corrupt historical logic (e.g., calculating old costs using the new facility’s overhead parameters). You must decouple the logical business code from the physical asset via an explicit mapping entity. Every transaction record must tie explicitly to an immutable, auto-generated warehouse_id primary key, rather than relying exclusively on a fluid businessUnitCode.
 - Soft Deletion and State Archiving: Never drop old records from production relational tables. Implement a soft-delete status or dedicated operational partitions (active vs. archived). This allows service logic to seamlessly bypass old locations for active logistics routing while allowing analytical reporting layers to scan the full historical timeline.

Questions & Considerations
----------------------------------
 - Physical Inventory Migration Costs: Determine if the physical transport fees required to truck millions of units from the old warehouse to the new warehouse are billed as capital expenses (CapEx) for the transition project or absorbed by the active operating budget.
 - Legal Financial Switchover Timing: Define the exact date and time the old warehouse stops accruing operational overhead and the new warehouse officially begins draining the ledger.
 - Active In-Flight Orders:** Establish how application services will allocate split labor and freight costs across the two separate warehouse instances if an order is placed while the old warehouse is packing it but shipping is executed out of the new warehouse.
 - Historical Metadata Retrofitting: Decide if archived history needs placeholder values backfilled to preserve report alignment if the new warehouse introduces a modern costing dimension (like micro-tracking shelf-space automation) that the old building lacked.
 ```



## Instructions for Candidates
Before starting the case study, read the [BRIEFING.md](BRIEFING.md) to quickly understand the domain, entities, business rules, and other relevant details.

**Analyze the Scenarios**: Carefully analyze each scenario and consider the tasks provided. To make informed decisions about the project's scope and ensure valuable outcomes, what key information would you seek to gather before defining the boundaries of the work? Your goal is to bridge technical aspects with business value, bringing a high level discussion; no need to deep dive.