import React from 'react';
import { Calendar, momentLocalizer } from "react-big-calendar";
import { Dimmer, Loader, Segment, Header, Item, Checkbox, Form, Divider, Message } from 'semantic-ui-react';
import ProposalListItem from './ProposalListItem';
import moment from 'moment';
import axios from 'axios';

// Setup the localizer by providing the moment (or globalize) Object
// to the correct localizer.
const localizer = momentLocalizer(moment); // or globalizeLocalizer


class ProposalList extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            uid: null,
            proposals: [],
            drafts: [],
            tmEvents: [],
            loading: false,
            error: null,
            listOldestFirst: false,
            calendarFinalizeFilter: 'all',
            calendarResponseFilter: 'all'
        };
        let { proposals, drafts, uid } = this.props;
        if (proposals) {
            try {
                this.state.proposals = JSON.parse(proposals);
            } catch (error) {
                console.log(error);
            }
        }
        if (drafts) {
            try {
                this.state.drafts = JSON.parse(drafts);
            } catch (error) {
                console.log(error);
            }
        }
        if (uid) {
            try {
                this.state.uid = parseInt(uid);
            } catch (error) {
                console.log(error);
            }
        }
    }

    handleEventDoubleClick = ({ url }) => {
        window.open(url, '_blank');
    }

    componentDidMount() {
        let eventKeys = this.state.proposals.map(proposal => proposal.finalizedEvent?.tmEventKey);
        if (eventKeys.length > 0) {
            this.setState({ loading: true });
            axios.get('/api/tm-api-proxy', {
                params: {
                    id: eventKeys.join(','),
                    size: eventKeys.length
                }
            })
                .then((response) => {
                    console.log('load tm events', response);
                    this.setState(({ proposals }) => ({
                        proposals: proposals.map(proposal => {
                            if (proposal.finalizedEvent === null) {
                                return proposal;
                            }
                            return {
                                ...proposal,
                                tmEventData: response.data?._embedded?.events
                                    ?.find(event => event.id === proposal.finalizedEvent.tmEventKey)
                            };
                        })
                        , loading: false
                    }));
                })
                .catch((error) => {
                    console.log(error);
                    this.setState({ loading: false, error });
                })
        }
    }

    getDateTime = (proposal) => {
        if (proposal.tmEventData) {
            return moment(proposal.tmEventData.dates.start.dateTime);
        }
        return moment.utc(proposal.createdAt);
    }

    handleToggleOldestFirst = (e, { checked }) => {
        this.setState({ listOldestFirst: checked });
    }

    handleChangeFinalizedFilter = (e, { value }) => {
        this.setState({ calendarFinalizeFilter: value });
    }

    handleChangeResponseFilter = (e, { value }) => {
        this.setState({ calendarResponseFilter: value });
    }

    render() {

        let {
            proposals,
            drafts,
            listOldestFirst,
            calendarFinalizeFilter,
            calendarResponseFilter,
            uid } = this.state;

        let listProposals = listOldestFirst ?
            [...proposals].sort((a, b) => this.getDateTime(a).diff(this.getDateTime(b)))
            : [...proposals].sort((a, b) => this.getDateTime(b).diff(this.getDateTime(a)));

        let calendarEvents = [];

        this.state.proposals.forEach(proposal => {

            // filters
            if (calendarFinalizeFilter == 'finalized' && proposal.finalizedEvent === null) {
                return;
            }
            if (calendarFinalizeFilter == 'ipOnly' && proposal.finalizedEvent !== null) {
                return;
            }
            const responded = proposal.pendingUsers.find(user => user.id === uid) === undefined;
            if (calendarResponseFilter == 'responded' && !responded) {
                return;
            }
            if (calendarResponseFilter == 'nrOnly' && responded) {
                return;
            }

            let start = this.getDateTime(proposal);
            let end = start.clone().add(2, 'hours');

            calendarEvents.push({
                id: proposal.id,
                title: proposal.title,
                allDay: false,
                start: start.toDate(),
                end: end.toDate(),
                tooltip: `Created by ${proposal.owner.username}, ${proposal.events.length} events`,
                url: `/proposal?id=${proposal.id}`
            });
        });

        const isFinalizedOptions = [
            { key: 1, text: 'All', value: 'all' },
            { key: 2, text: 'Finalized only', value: 'finalized' },
            { key: 3, text: 'In progress only', value: 'ipOnly' },
        ];

        const hasRespondedOptions = [
            { key: 1, text: 'All', value: 'all' },
            { key: 2, text: 'Responded only', value: 'responded' },
            { key: 3, text: 'Not responded only', value: 'nrOnly' },
        ];

        return (
            <Segment.Group>
                <Segment>
                    <Dimmer active={this.state.loading} inverted>
                        <Loader>Loading Proposals</Loader>
                    </Dimmer>
                    <Header as='h2' content='Proposals' />
                    <Checkbox toggle label='oldest proposal first' onChange={this.handleToggleOldestFirst} />
                    <Divider />
                    {listProposals.length > 0 &&
                        <Item.Group divided relaxed>
                            {listProposals.map(proposal => <ProposalListItem proposal={proposal} key={proposal.id} />)}
                        </Item.Group>
                    }
                    {listProposals.length === 0 &&
                        <Message header='No proposals' content='You have no active proposals.' />
                    }
                    <Divider />
                    <Form>
                        <Form.Group widths='2'>
                            <Form.Dropdown
                                fluid
                                label='Filter by Proposal State'
                                options={isFinalizedOptions}
                                selection
                                fluid
                                value={calendarFinalizeFilter}
                                onChange={this.handleChangeFinalizedFilter}
                            />
                            <Form.Dropdown
                                fluid
                                label='Filter by My Response'
                                options={hasRespondedOptions}
                                selection
                                fluid
                                value={calendarResponseFilter}
                                onChange={this.handleChangeResponseFilter}
                            />
                        </Form.Group>
                    </Form>
                    <Calendar
                        localizer={localizer}
                        events={calendarEvents}
                        startAccessor="start"
                        endAccessor="end"
                        tooltipAccessor="tooltip"
                        onDoubleClickEvent={this.handleEventDoubleClick}
                    />
                </Segment>
                <Segment>
                    <Header as='h2' content='Drafts' />
                    {drafts.length > 0 &&
                        <Item.Group divided relaxed>
                            {drafts.map(draft => (
                                <Item key={draft.id}>
                                    <Item.Content>
                                        <Item.Header as='a' href={`/create-proposal?draft-id=${draft.id}`} content={draft.title} />
                                        <Item.Extra>
                                            <a href={`/create-proposal?draft-id=${draft.id}`}>Edit</a>
                                        </Item.Extra>
                                    </Item.Content>
                                </Item>
                            ))}

                        </Item.Group>
                    }
                    {drafts.length === 0 &&
                        <Message header='No drafts' content='You have no proposal drafts.' />
                    }
                </Segment>
            </Segment.Group>
        );
    }
}

export default ProposalList;