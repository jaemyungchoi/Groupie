import React from 'react';
import axios from 'axios';
import { Button, Input, Item, Pagination, Message, Header, Segment, Divider, Form, Label, Icon, Confirm, List } from 'semantic-ui-react'
import EventItem from './EventItem';
import InviteUserForm from './InviteUserForm';
import { DateInput } from 'semantic-ui-calendar-react';
import moment from 'moment';

const pageSize = 5;
const maxEvents = 1000; // TM API limit
const maxPageCount = Math.trunc(maxEvents / pageSize);
const dateFormat = 'MM/DD/YYYY';

class EventSearch extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            proposalTitle: '',
            invitedUsernames: [],
            selectedEvents: [],
            searchParams: null,
            searchResults: [],
            searchText: '',
            error: null,
            loading: false,
            currentPage: 0,
            totalPages: 0,
            preloadEventKeys: [],
            city: '',
            startDate: '',
            endDate: '',
            category: '',
            inviteUserError: null,
            removeEventId: null,
            removeUsername: null,
            submitErrors: [],
            creatingProposal: false,
            savingDraft: false,
            oldDraftId: null,
            draftError: null
        };
        if (props.draft) {
            let draft = JSON.parse(props.draft);
            this.state.proposalTitle = draft.title ?? '';
            this.state.invitedUsernames = draft.users ?? [];
            this.state.preloadEventKeys = draft.events ?? [];
        }
        if (props.draftId) {
            this.state.oldDraftId = parseInt(props.draftId);
        }
        if (props.error) {
            this.state.draftError = props.error;
        }
    }

    handleFieldChange = (event, { name, value }) => {
        if (this.state.hasOwnProperty(name)) {
            this.setState({ [name]: value });
        }
    }

    doSearch = (params) => {
        this.setState({ loading: true });
        let searchParams = {
            ...(this.state.searchParams ?? {}),
            ...params,
        };
        axios.get('/api/tm-api-proxy', {
            params: {
                ...searchParams,
                size: pageSize
            }
        })
            .then((response) => {
//                console.log(response);
                this.setState({
                    loading: false,
                    searchParams,
                    searchResults: response.data?._embedded?.events ?? [],
                    currentPage: response.data?.page?.number ?? 0,
                    totalPages: response.data?.page?.totalPages ?? 0,
                    error: null
                })
            })
            .catch((error) => {
                console.log(error);
                this.setState({
                    loading: false,
                    searchResults: [],
                    error
                })
            })
    }

    handleSearch = () => {
        let { searchText, startDate, endDate, city, category } = this.state;
        let searchParams = {
            keyword: searchText,
            startDateTime: startDate !== '' ? moment(startDate, dateFormat).format() : undefined,
            endDateTime: endDate !== '' ? moment(endDate, dateFormat).format() : undefined,
            city: city !== '' ? city : undefined,
            segmentName: category !== '' ? category : undefined,
            page: 0
        };
        return this.doSearch(searchParams)
    }

    handlePageChange = (e, { activePage }) => {
        return this.doSearch({ page: activePage - 1 })
    }

    handleAddEvent = (event) => {
        this.setState((state) => {
            let { selectedEvents } = state;
            if (selectedEvents.map(event => event?.id).includes(event?.id)) {
                return {};
            } else {
                return { selectedEvents: [...selectedEvents, event] };
            }
        })
    }

    handleShowEventConfirm = (id) => {
        this.setState({ removeEventId: id });
    }

    handleShowUserConfirm = (username) => {
        this.setState({ removeUsername: username });
    }

    handleHideEventConfirm = () => {
        this.setState({ removeEventId: null });
    }

    handleHideUserConfirm = () => {
        this.setState({ removeUsername: null });
    }

    handleRemoveEvent = () => {
        this.setState((state) => ({
            selectedEvents: state.selectedEvents.filter(event => event?.id !== this.state.removeEventId),
            removeEventId: null
        }));
    }

    handleAddUser = (value) => {
        this.setState((state) => {
            let { invitedUsernames } = state;
            if (invitedUsernames.includes(value)) {
                return { inviteUserError: `${value} is already invited.` };
            } else {
                return { invitedUsernames: [...invitedUsernames, value], inviteUserError: null };
            }
        })
    }

    handleRemoveUser = () => {
        this.setState((state) => ({
            invitedUsernames: state.invitedUsernames.filter(invitedUsernames => invitedUsernames != this.state.removeUsername),
            removeUsername: null
        }));
    }

    handleInviteUserError = (error) => {
        console.log(error);
        this.setState({ inviteUserError: error });
    }

    handleCreateProposal = () => {
        let errors = [];
        let { proposalTitle, invitedUsernames, selectedEvents, oldDraftId } = this.state;
        if (proposalTitle === '') {
            errors.push('Please fill out the proposal title.');
        }
        if (invitedUsernames.length === 0) {
            errors.push('Please invite at least one user.');
        }
        if (selectedEvents.length === 0) {
            errors.push('Please select at least one event.');
        }
        if (errors.length > 0) {
            this.setState({ submitErrors: errors });
        } else {
            let body = {
                title: proposalTitle,
                users: invitedUsernames,
                events: selectedEvents.map(event => event?.id),
                oldDraftId
            }
            this.setState({ creatingProposal: true });
            axios.post('/api/post-proposal', body)
                .then(response => {
                    window.location.replace(`/proposal?id=${response.data.id}`);
                })
                .catch(error => {
                    console.log(error);
                    this.setState({
                        submitErrors: [String(error)],
                        creatingProposal: false
                    });
                });
        }
    }

    handleSaveDraft = () => {
        let { proposalTitle, invitedUsernames, selectedEvents, oldDraftId } = this.state;
        let body = {
            title: proposalTitle,
            users: invitedUsernames,
            events: selectedEvents.map(event => event?.id),
            oldDraftId
        }
        this.setState({ savingDraft: true });
        axios.post('/api/proposal-draft', body)
            .then(response => {
                this.setState({
                    oldDraftId: response.data.id,
                    savingDraft: false
                });
            })
            .catch(error => {
                console.log(error);
                this.setState({
                    submitErrors: [String(error)],
                    savingDraft: false
                });
            });
    }

    componentDidMount() {
        let { preloadEventKeys } = this.state;
        if (preloadEventKeys.length > 0) {
            this.setState({ loading: true });
            axios.get('/api/tm-api-proxy', {
                params: {
                    id: preloadEventKeys.join(',')
                }
            })
                .then((response) => {
//                    console.log('preload', response);
                    this.setState(state => {
                        let { selectedEvents } = state;
                        let selectedIDs = selectedEvents.map(event => event?.id);
                        let newSelectedEvents = [...selectedEvents];
                        for (let event of response.data?._embedded?.events ?? []) {
                            if (!selectedIDs.includes(event?.id)) {
                                newSelectedEvents.push(event);
                            }
                        }
                        return { loading: false, selectedEvents: newSelectedEvents };
                    });
                })
                .catch((error) => {
                    console.log(error);
                    this.setState({
                        loading: false,
                        error
                    })
                })
        }
    }

    render() {
        let { proposalTitle,
            invitedUsernames,
            loading,
            searchResults,
            currentPage,
            totalPages,
            error,
            searchParams,
            selectedEvents,
            startDate,
            endDate,
            inviteUserError,
            removeEventId,
            removeUsername,
            submitErrors,
            creatingProposal,
            savingDraft,
            oldDraftId,
            draftError } = this.state;
        let selectedIDs = selectedEvents.map(event => event?.id);
        const categoryOptions = [
            { key: 'sports', value: 'Sports', text: 'Sports', icon: 'football ball' },
            { key: 'music', value: 'Music', text: 'Music', icon: 'music' },
            { key: 'a&t', value: 'Arts & Theatre', text: 'Arts & Theatre', icon: 'paint brush' },
            { key: 'film', value: 'Film', text: 'Film', icon: 'film' },
            { key: 'misc', value: 'Miscellaneous', text: 'Miscellaneous', icon: 'ellipsis horizontal' },
            { key: 'undef', value: 'Undefined', text: 'Undefined', icon: 'question' },
        ]
        return <Segment.Group>
            <Segment>
                <Header content='Create Proposal' />
                <Input placeholder='Proposal Title' fluid onChange={this.handleFieldChange} name='proposalTitle' value={proposalTitle} />
            </Segment>
            <Segment>
                <Header content='Invite User' />
                <Label.Group>
                    {invitedUsernames.map(name =>
                        <Label key={name} size='large'>
                            {name}
                            <Icon name='delete' onClick={() => this.handleShowUserConfirm(name)} />
                        </Label>)}
                </Label.Group>
                {inviteUserError && <Message content={inviteUserError} error />}
                <InviteUserForm onSelect={this.handleAddUser} onError={this.handleInviteUserError} />
            </Segment>
            <Segment>
                <Header content='Pick Events' />
                <Form onSubmit={this.handleSearch}>
                    <Form.Input
                        fluid
                        label='Keywords'
                        placeholder='Any Event'
                        name='searchText'
                        onChange={this.handleFieldChange}
                        action={<Button
                            content='Search'
                            loading={loading}
                            disabled={loading}
                            type='submit' />} />
                    <Form.Group widths='equal'>
                        <Form.Input
                            fluid
                            label='City'
                            name='city'
                            placeholder='Any City'
                            onChange={this.handleFieldChange} />
                        <Form.Field>
                            <label>Start Date</label>
                            <DateInput
                                placeholder='Any Start Date'
                                name='startDate'
                                value={startDate}
                                iconPosition='right'
                                dateFormat={dateFormat}
                                closable
                                clearable
                                onChange={this.handleFieldChange}
                                maxDate={endDate}
                                initialDate={endDate !== '' ? moment(endDate, dateFormat) : null}
                            />
                        </Form.Field>
                        <Form.Field>
                            <label>End Date</label>
                            <DateInput
                                placeholder='Any End Date'
                                name='endDate'
                                value={endDate}
                                iconPosition='right'
                                dateFormat={dateFormat}
                                closable
                                clearable
                                onChange={this.handleFieldChange}
                                minDate={startDate}
                                initialDate={startDate !== '' ? moment(startDate, dateFormat) : null}
                            />
                        </Form.Field>
                        <Form.Dropdown
                            id='category-dropdown'
                            fluid
                            label='Genre'
                            name='category'
                            placeholder='Any Genre'
                            search
                            selection
                            clearable
                            options={categoryOptions}
                            onChange={this.handleFieldChange} />
                    </Form.Group>
                </Form>
                {error && <Message error content={String(error)} />}
                <Item.Group divided id='search-results' >
                    {searchResults.map(event => <EventItem data={event} key={event.id} extraWidget={
                        <Button
                            content='Add'
                            floated='right'
                            color='green'
                            onClick={() => this.handleAddEvent(event)}
                            disabled={selectedIDs.includes(event?.id)} />
                    } />)}
                </Item.Group>
                {searchParams !== null && searchResults.length === 0 &&
                    <Message>
                        <Message.Header>No Event Found</Message.Header>
                        <p>Try changing the keywords and try again.</p>
                    </Message>}
                {searchParams && totalPages > 0
                    && <Pagination
                        activePage={currentPage + 1}
                        onPageChange={this.handlePageChange}
                        totalPages={Math.min(totalPages, maxPageCount)}
                        disabled={loading}
                        prevItem={null}
                        nextItem={null}
                        stackable />}
                {selectedEvents.length > 0 &&
                    <div>
                        <Divider />
                        <Header content='Selected Events' />
                        <Item.Group divided id='selected-events' >
                            {selectedEvents.map(event =>
                                <EventItem
                                    data={event}
                                    key={event.id}
                                    extraWidget={
                                        <Button
                                            content='Remove'
                                            floated='right' color='red'
                                            onClick={() => this.handleShowEventConfirm(event?.id)} />
                                    }
                                />)}
                        </Item.Group>
                    </div>}
                <Confirm
                    open={removeEventId !== null}
                    header='Remove Event'
                    content={`${selectedEvents.find(event => event?.id === removeEventId)?.name} will be removed from the proposal.`}
                    onCancel={this.handleHideEventConfirm}
                    onConfirm={this.handleRemoveEvent}
                />
                <Confirm
                    open={removeUsername !== null}
                    header='Remove Invited User'
                    content={`${removeUsername} will be removed from the proposal.`}
                    onCancel={this.handleHideUserConfirm}
                    onConfirm={this.handleRemoveUser}
                />
            </Segment>
            <Segment>
                {submitErrors.length > 0 &&
                    <Message error>
                        <List bulleted>
                            {submitErrors.map((error, i) => <List.Item content={error} key={i} />)}
                        </List>
                    </Message>
                }
                {draftError && <Message error content={draftError} />}
                {oldDraftId !== null &&
                    <Message>
                        <Message.Header>
                            Draft Saved
                        </Message.Header>
                        You can use <a href={`/create-proposal?draft-id=${oldDraftId}`}>this link</a> to edit the draft later.
                    </Message>
                }
                <Button
                    content='Create'
                    color='green'
                    onClick={this.handleCreateProposal}
                    disabled={creatingProposal}
                    loading={creatingProposal} />
                <Button
                    content='Save As Draft'
                    onClick={this.handleSaveDraft}
                    disabled={savingDraft}
                    loading={savingDraft} />
            </Segment>
        </Segment.Group>;
    }
}

export default EventSearch;